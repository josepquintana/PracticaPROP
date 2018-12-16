package domini;

import dades.ControladorDades;

import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ControladorDomini implements Cloneable
{
    private ControladorDades controladorDades;

    private String nomCentre;
    private PeriodeLectiu periodeLectiu;
    private JornadaLectiva jornadaLectiva;
    private Aules aules;
    private PlansDeEstudis plansDeEstudis;
    private Horari horari;

    public ControladorDomini() throws IOException {
        this.controladorDades   = new ControladorDades();
        this.nomCentre          = new String();
        this.periodeLectiu      = new PeriodeLectiu();
        this.jornadaLectiva     = new JornadaLectiva();
        this.aules              = new Aules();
        this.plansDeEstudis     = new PlansDeEstudis();
        this.horari             = new Horari();
    }

    public ControladorDomini(String nomCentre, PeriodeLectiu periodeLectiu, JornadaLectiva jornadaLectiva) throws IOException {
        this.controladorDades   = new ControladorDades();
        this.nomCentre          = nomCentre;
        this.periodeLectiu      = new PeriodeLectiu(periodeLectiu);
        this.jornadaLectiva     = new JornadaLectiva(jornadaLectiva);
        this.aules              = new Aules();
        this.plansDeEstudis     = new PlansDeEstudis();
        this.horari             = new Horari();
    }

    public ControladorDomini(String nomCentre, PeriodeLectiu periodeLectiu, JornadaLectiva jornadaLectiva, Aules aules, PlansDeEstudis plansDeEstudis) throws IOException {
        this.controladorDades   = new ControladorDades();
        this.nomCentre          = nomCentre;
        this.periodeLectiu      = new PeriodeLectiu(periodeLectiu);
        this.jornadaLectiva     = new JornadaLectiva(jornadaLectiva);
        this.aules              = new Aules(aules);
        this.plansDeEstudis     = new PlansDeEstudis(plansDeEstudis);
        this.horari             = new Horari();
    }

    public ControladorDomini(ControladorDomini cd) {
        this.controladorDades = cd.getControladorDades();
        this.nomCentre      = cd.getNomCentre();
        this.periodeLectiu  = new PeriodeLectiu(cd.getPeriodeLectiu());
        this.jornadaLectiva = new JornadaLectiva(cd.getJornadaLectiva());
        this.aules          = new Aules(cd.getAules());
        this.plansDeEstudis = new PlansDeEstudis(cd.getPlansDeEstudis());
        this.horari         = new Horari(cd.getHorari());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ControladorDomini cd;
        try {
            cd = (ControladorDomini) super.clone();

            // mutable methods!
            cd.setPeriodeLectiu((PeriodeLectiu) this.getPeriodeLectiu().clone());
            cd.setJornadaLectiva((JornadaLectiva) this.getJornadaLectiva().clone());
            cd.setAules((Aules) this.getAules().clone());
            cd.setPlansDeEstudis((PlansDeEstudis) this.getPlansDeEstudis().clone());
            cd.setHorari((Horari) this.getHorari().clone());
//            cd.setControladorDades((ControladorDades) this.getControladorDades().clone());
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return cd;
    }

    public void loadData() throws IOException, MyException {
        String centreDocent = controladorDades.loadCentreDocent();
        ArrayList<String> plansDeEstudis = controladorDades.loadPlansDeEstudis();
        ArrayList<String> aules = controladorDades.loadAules();
        ArrayList<String> assignatures = controladorDades.loadAssignatures();

        Parser.centreDocent(centreDocent, this);

        for (String str : plansDeEstudis) {
            PlaEstudis plaEstudis = Parser.plaEstudis(str, this.jornadaLectiva);
            this.plansDeEstudis.afegirPlaEstudis(plaEstudis);
        }

        for (String str : aules) {
            Aula aula = Parser.aula(str);
            this.aules.afegirAula(aula);
        }

        for (String str : assignatures) {
            String nomPlaEstudis = str.split(", ")[1];
            Assignatura assignatura = Parser.assignatura(str);
            this.plansDeEstudis.getPlaEstudis(nomPlaEstudis).afegirAssignaturaAlPlaEstudis(assignatura);
        }
    }

    public void storeData() throws IOException, MyException, InterruptedException {
        String centreDocent = Serializer.centreDocent(this);
        controladorDades.saveCentreDocent(centreDocent);

        ArrayList<String> plansDeEstudis = Serializer.plansDeEstudis(this.plansDeEstudis);
        controladorDades.savePlansDeEstudis(plansDeEstudis);

        ArrayList<String> aules = Serializer.aules(this.aules);
        controladorDades.saveAules(aules);

        for (PlaEstudis pe : this.plansDeEstudis.getPlansDeEstudis()) {
            ArrayList<String> assignatures = Serializer.assignatures(pe.getAssignatures(), pe.getNomPla());
            controladorDades.saveAssignatures(assignatures);
        }
    }

    public void generateHorariPlaEstudis(int numPla) throws CloneNotSupportedException {
        horari = new Horari(this.getPlaEstudis(numPla).getJornadaLectiva(), this.aules.mida());
        horari.generarHorari(this.getPlaEstudis(numPla).getAssignatures(), this.aules);
        this.guardarHorariAlPlaEstudis(numPla);
    }

    /**
     * @param numPla
     * @throws CloneNotSupportedException
     *
     * Cal cridar sempre que es modifiqui l'horari del ControladorDomini
     *
     */
    private void guardarHorariAlPlaEstudis(int numPla) throws CloneNotSupportedException {
        if (!horari.empty()) {
            this.plansDeEstudis.getPlaEstudis(numPla).setHorari((Horari) this.horari.clone());
        }
    }

    public boolean afegirPlaEstudis(PlaEstudis pe) {
        return this.plansDeEstudis.afegirPlaEstudis(pe);
    }

    public boolean eliminarPlaEstudis(PlaEstudis pe) {
        return this.plansDeEstudis.eliminarPlaEstudis(pe);
    }

    public boolean afegirAula(Aula a) throws MyException { return this.aules.afegirAula(a); }

    public boolean eliminarAula(Aula a) throws MyException { return this.aules.eliminarAula(a); }

    public void setNomCentre(String nomCentre) {
        this.nomCentre = new String(nomCentre);
    }

    public void setPeriodeLectiu(PeriodeLectiu periodeLectiu) {
        this.periodeLectiu = new PeriodeLectiu(periodeLectiu);
    }

    public void setJornadaLectiva(JornadaLectiva jornadaLectiva) {
        this.jornadaLectiva = new JornadaLectiva(jornadaLectiva);
    }

    public void setPlansDeEstudis(PlansDeEstudis plansDeEstudis) {
        this.plansDeEstudis = plansDeEstudis;
    }

    public void setAules(Aules aules) {
        this.aules = aules;
    }

    public void setHorari(Horari horari) {
        this.horari = horari;
    }

    public void setControladorDades(ControladorDades controladorDades) {
        this.controladorDades = controladorDades;
    }

    public String getNomCentre() {
        return this.nomCentre;
    }

    public PeriodeLectiu getPeriodeLectiu() {
        return this.periodeLectiu;
    }

    public JornadaLectiva getJornadaLectiva() {
        return this.jornadaLectiva;
    }

    public PlansDeEstudis getPlansDeEstudis() {
        return this.plansDeEstudis;
    }

    public PlaEstudis getPlaEstudis(String nomPla) {
        for (int i = 0; i < this.plansDeEstudis.mida(); i++) {
            if (this.plansDeEstudis.getPlaEstudis(i).getNomPla().equals(nomPla)) return this.plansDeEstudis.getPlaEstudis(i);
        }
        return null;
    }

    public PlaEstudis getPlaEstudis(int i) {
        return this.plansDeEstudis.getPlaEstudis(i);
    }

    public Aules getAules() {
        return aules;
    }

    public Horari getHorari() {
        return horari;
    }

    public ControladorDades getControladorDades() {
        return controladorDades;
    }

    // Funcions temporals que no PRESENTAREM!!

    public void printCentreDocentLong() throws MyException {
        System.out.println("\n> CentreDocent [Long Format]:\n");
        System.out.println(" nomCentre: " + this.nomCentre + "\n");
        this.periodeLectiu.printPeriodeLectiu();                System.out.println("");
        this.jornadaLectiva.printJornadaLectivaLong();          System.out.println("");
        this.aules.printAulesLong(1);    System.out.println("");
        this.plansDeEstudis.printPlansDeEstudisLong();          System.out.println("");
        System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
    }

    public void printCentreDocent() throws MyException {
        System.out.println("\n> CentreDocent:");
        System.out.println(" nomCentre: " + this.nomCentre);
        this.periodeLectiu.printPeriodeLectiu();
        this.jornadaLectiva.printJornadaLectivaLong();
        this.aules.printAules(1);
        this.plansDeEstudis.printPlansDeEstudis();
        System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
    }

    private String getDia(int i) {
        if (i == 0) return ("\n\n  >>>>>                                              D   I   L   L   U   N   S                                             <<<<<\n");
        if (i == 1) return ("\n\n  >>>>>                                              D   I   M   A   R   T   S                                             <<<<<\n");
        if (i == 2) return ("\n\n  >>>>>                                            D   I   M   E   C   R   E   S                                           <<<<<\n");
        if (i == 3) return ("\n\n  >>>>>                                                D   I   J   O   U   S                                               <<<<<\n");
        if (i == 4) return ("\n\n  >>>>>                                          D   I   V   E   N   D   R   E   S                                         <<<<<\n");
        return "Error";
    }

    private String getHora(int j) {
        String str = "  ";
        if (horari.getHIni() + j  < 10) str += " ";
        str += (horari.getHIni() + j) + " - ";
        if (horari.getHIni() + j + 1 < 10) str += " ";
        str += ((horari.getHIni() + j + 1) + "h :  ");
        return str;
    }

    public void printHorari(int numPla) throws CloneNotSupportedException, MyException {

        if (numPla >= this.plansDeEstudis.mida()) { throw new MyException("No existeix el pla d'estudis num " + numPla + "."); }
        if (this.horari.empty()) throw new MyException("Encara no s'ha genereat l'horari per aquest pla d'estudis.");
        this.horari = (Horari) this.plansDeEstudis.getPlaEstudis(numPla).getHorari().clone();

        for (int i = 0; i < this.horari.getDies(); i++) {
            System.out.println(getDia(i));
            for (int j = 0; j < this.horari.getHores(); j++) {
                System.out.print(getHora(j));
                for (int k = 0; k < this.horari.getN_aules(); k++) {
                    if(null != horari.getAssignacioIJK(i,j,k))  System.out.print(horari.getAssignacioIJK(i,j,k).getAssignacioPrintFormat());
                    else System.out.print("      ---        ");
                    System.out.print("\t");
                }
                System.out.print("\n");
            }
            System.out.print("\n");
        }
        System.out.println("\n");
    }

    public void printHorariIJK(int numPla) throws CloneNotSupportedException {

        this.horari = (Horari) this.plansDeEstudis.getPlaEstudis(numPla).getHorari().clone();

        int used_aules = 0;

        for (int i = 0; i < this.horari.getDies(); i++) {
            for (int j = 0; j < this.horari.getHores(); j++) {
                for (int k = 0; k < this.horari.getN_aules(); k++) {
                    System.out.print("Horari[" + i + "][" + j + "][" + k + "] \t = ");
                    if(null != horari.getAssignacioIJK(i,j,k)) { horari.getAssignacioIJK(i,j,k).printAssignacio(); used_aules++; }
                    else System.out.println("[none]");
                }
            }
        }
        System.out.println("\n\n");
        printUsedAules(used_aules);
        System.out.println("\n\n");
    }

    public void printUsedAules(int ua) {

        System.out.println("Assigned " + ua + "/" + (this.aules.mida()*this.horari.getHores()*5) + " of possible [dia][hora][aula].");

    }
    
    //FUNCIONS PER COMUNICAR-SE AMB PRESENTACIÓ
    
    public void crearAula(String nom, String capacitat, String laboratori) throws MyException{
        Aula a = new Aula(nom, Integer.parseInt(capacitat), Boolean.parseBoolean(laboratori));
        this.aules.afegirAula(a);
    }
    
    public void crearAssig(String codi, String nom, String laboratori, String credits, String nivell, String correq, String grups){
        Assignatura A = new Assignatura();
        this.plansDeEstudis.getPlaEstudis(0).afegirAssignaturaAlPlaEstudis(A);
    }
    
    public void modificarAula(String codi, String nom, String capacitat, String laboratori) throws MyException{
        int mida = this.aules.mida();
        for (int i = 0; i < mida; ++i){
            if (aules.getAula(i).getCodi().equals(codi)){
                if ("".equals(nom)) {
                } 
                else {
                    aules.getAula(i).setCodi(nom);
                }
                if ("".equals(capacitat)) {
                } else {
                    aules.getAula(i).setCapacitat(Integer.parseInt(capacitat));
                }
                if ("".equals(laboratori)) {
                } else {
                    aules.getAula(i).setPCs(Boolean.parseBoolean(laboratori));
                }
            }
        }
    }
    
    public void eliminarAula(String codi) throws MyException{
        int mida = this.aules.mida();
        for (int i = 0; i < mida; ++i){
            if (aules.getAula(i).getCodi().equals(codi)){
                aules.eliminarAula(i);
            }
        }
    }
    
    public String[] getNomAules(){
        
        int mida = this.aules.mida();
        String[] noms;
        noms = new String[mida];

        for (int i = 0; i < mida; ++i){
            noms[i] = (aules.getAula(i).getCodi());
        }
        return noms;
    }

    
    
    public int getNumAules(){
        return aules.mida();
    }
    
    public String getCapacitat(String codi){
       for (int i = 0; i < aules.mida(); ++i){
           if (aules.getAula(i).getCodi() == codi){
               return Integer.toString(aules.getAula(i).getCapacitat());
           }
       }
    return "";
    }
    
    public String getLab(String codi){
       for (int i = 0; i < aules.mida(); ++i){
           if (aules.getAula(i).getCodi() == codi){
               return Boolean.toString(aules.getAula(i).isLab());
           }
       }
    return "";
    }
    
    public void generarHorari(){
        
    }
    
    public String getHoraIni(){
        DateFormat df = new SimpleDateFormat("HH:mm ");
        Time t1 = this.jornadaLectiva.getHoraIni();
        String d =  df.format(t1);
        return d;        
    }
    
    public String getHoraFi(){
        DateFormat df = new SimpleDateFormat("HH:mm ");
        Time t1 = this.jornadaLectiva.getHoraFi();
        String d =  df.format(t1);
        return d;        
    }
    
    public String getDataIni(){
        DateFormat df = new SimpleDateFormat("DD/mm/mm ");
        Date d1 = this.periodeLectiu.getDataIni();
        String d = df.format(d1);
        return d;
    }
    
    public String getDataFi(){
        DateFormat df = new SimpleDateFormat("DD/mm/mm ");
        Date d1 = this.periodeLectiu.getDataFi();
        String d = df.format(d1);
        return d;
    }
    
    public String getNomPla(){
        return this.plansDeEstudis.getPlaEstudis(0).getNomPla();
    }
    
    public String getNomTitulacio(){
        return this.plansDeEstudis.getPlaEstudis(0).getTitulacio().getNomTitulacio();
    }
    
    public String getTipusTitulacio(){
        return this.plansDeEstudis.getPlaEstudis(0).getTitulacio().getTipusTitulacio();
    }
    
    public void modificarCalendari(String jornada, String periode) throws ParseException{
        String s1 = null, s2 = null, s3 = null, s4 = null;
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        DateFormat formatter2 = new SimpleDateFormat("DD/mm/mm");
        Time t1 = null;
        Time t2 = null;
        Date d1 = null;
        Date d2 = null;
        if (!"".equals(jornada)){
            s1 = jornada.substring(0, 5);
            s2 = jornada.substring(6, 11);
            t1 = new Time(formatter.parse(s1).getTime());
            t2 = new Time(formatter.parse(s2).getTime());
            this.jornadaLectiva.setHoraIni(t1);
            this.jornadaLectiva.setHoraFi(t2);
        }
        if (!"".equals(periode)){
            s3 = periode.substring(0, 8);
            s4 = periode.substring(9, 17);
            d1 = formatter2.parse(s3);
            d2 = formatter2.parse(s4);
            this.periodeLectiu.setDataIni(d1);
            this.periodeLectiu.setDataFi(d2); 
        }                        
    }
    
    public void modificarPla(String nom, String titulacio, String tipus){
        this.plansDeEstudis.getPlaEstudis(0).setNomPla(nom);
        this.plansDeEstudis.getPlaEstudis(0).getTitulacio().setNom(titulacio);
        this.plansDeEstudis.getPlaEstudis(0).getTitulacio().setTipus(tipus);
    }
    
    public void afegirPla(String nom, String titulacio, String tipus){
        Titulacio t = new Titulacio(titulacio, tipus);
        PlaEstudis pe = new PlaEstudis(nom, this.jornadaLectiva, t);
        this.plansDeEstudis.afegirPlaEstudis(pe);
    }

}

