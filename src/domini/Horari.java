package domini;

import java.util.ArrayList;

public class Horari implements Cloneable
{
    private final int dies = 5;
    private int hIni;
    private int hores;
    private HoraLectiva[][] setmana;

    @SuppressWarnings("deprecation")
    public Horari(JornadaLectiva jL) {
        this.hIni = jL.getHoraIni().getHours();
        this.hores = (jL.getHoraFi().getHours())-hIni;
        this.setmana = new HoraLectiva[dies][hores];
        this.iniSetmana();  // s'ha de inicialitzar, sino peta molt potent
    }

    public Horari(Horari horari) {
        this.hores = horari.getHores();
        this.setmana = new HoraLectiva[this.dies][this.hores];
    }

    //@Override
    protected Object clone(JornadaLectiva jL) throws CloneNotSupportedException {
        JornadaLectiva aux = (JornadaLectiva) jL.clone();
        Horari horari = new Horari(aux);
        try {
            horari = (Horari) super.clone();

            for (int i = 0; i < this.dies; i++) {
                for (int j = 0; j < this.hores; j++) {
                    horari.setHoraLectiva((HoraLectiva) this.getHoraLectiva(i, j).clone(), i, j);
                }
            }
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return horari;
    }

    public void GenerarHorari(Assignatures assignaturesPlaEstudis, Aules aulesCentreDocent) throws MyException, CloneNotSupportedException {
        Assignatures assignatures = (Assignatures) assignaturesPlaEstudis.clone();
        Assignatures assignaturesPE = (Assignatures) assignatures.clone();
        Aules aules = (Aules) aulesCentreDocent.clone();

        ArrayList<HoraLectiva> horesLectives = new ArrayList<>();
        //assignatures.printAssignaturesLong();
        boolean erroni = false;
        while (!assignatures.esBuit() && !erroni) {
            HoraLectiva hL = GeneradorHora.GenerarHoraLectiva(assignatures, aules, assignaturesPE);
            //eliminarAssignacions(assignatures,hL);
            horesLectives.add(hL);
            //hL.printHoraLectiva();
            if(hL.esBuit()) {
                erroni = true;
                System.out.println("erroni!");
            }

        }

        if(horesLectives.size() > this.hores*this.dies || erroni) {
            if(erroni) System.out.println("\nError: Hi ha alguna assignatura:grup que no hi cap a cap aula");
            else System.out.println("\nError: No hi ha espai suficient per tantes horesLectives");
        }
        else this.OmplirHorari(horesLectives);

    }

    private void OmplirHorari(ArrayList<HoraLectiva> horesLectives) {
        for (int i = 0; i < this.hores; i++) {
            for (int j = 0; j < this.dies; j++) {
                if(!horesLectives.isEmpty()){
                    HoraLectiva hL = horesLectives.remove(0);
                    this.setmana[j][i] = new HoraLectiva(hL);
                }
                else break;
            }
        }
    }

    public boolean existeixHoraLectiva(HoraLectiva hL) {
        for(int i = 0; i < dies; ++i) {
            for(int j = 0; j < hores; ++j) {
                if(this.setmana[i][j].equals(hL)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean afegirHoraLectiva(HoraLectiva hL) {
        if(existeixHoraLectiva(hL)){
            System.out.println(">>> afegirHora(): L' hora lectiva donada ja existeix");  return false;
        }
        for(int i = 0; i < dies; ++i) {
            for(int j = 0; j < hores; ++j) {
                if(this.setmana[i][j].esBuit()) {
                    this.setmana[i][j] = hL;
                    return true;
                }
            }
        }
        System.out.println(">>> afegirHora(): No queden horesLectives disponibles per assignar"); return false;
    }

    public boolean afegirHoraLectiva(HoraLectiva hL, int dia, int hora) {
        if(existeixHoraLectiva(hL)){
            System.out.println(">>> afegirHora_ij(): L' hora lectiva donada ja existeix"); return false;
        }
        hora = hora - this.hIni;
        if (hora < 0 || hora >= this.hores || dia < 0 || dia >= 5) {
            System.out.println(">>> afegirHora_ij(): Parametres hora i dia no valids."); return false;
        }
        if (!this.setmana[dia][hora].esBuit()) {
            System.out.println(">>> afegirHora_ij(): setmana[i][j] no esta buida."); return false;
        }
        this.setmana[dia][hora] = hL;
        return true;
    }

    public boolean eliminarHoraLectiva(HoraLectiva hL){
        for(int i = 0; i < dies; ++i) {
            for(int j = 0; j < hores; ++j) {
                if(this.setmana[i][j].equals(hL)) {
                    this.setmana[i][j] = new HoraLectiva();
                    return true;
                }
            }
        }
        System.out.println(">>> eliminarHoraLectiva(): L' hora lectiva donada no existeix");
        return false;
    }

    public boolean eliminarHoraLectiva(int dia, int hora) {
        hora = hora - this.hIni;
        if (hora >= 0 && hora < this.hores && dia >= 0 && dia < 5) {
            this.setmana[dia][hora] = new HoraLectiva();
            return true;
        }
        else {
            System.out.println(">>> eliminarHora_ij(): Parametres hora i dia no valids.");
            return false;
        }
    }

    public HoraLectiva[][] getSetmana() {
        return setmana;
    }

    public HoraLectiva getHoraLectiva(int dia, int hora) {
        if (hora < 0 || hora >= this.hores || dia < 0 || dia >= 5) {
            System.out.println(">>> getHoraLectiva(): Parametres hora i dia no valids.");
            return null;
        }
        return this.setmana[dia][hora];
    }

    public int getDies() {
        return dies;
    }

    public int getHores() {
        return hores;
    }

    public int getHIni() { return hIni; }

    public void setSetmana(HoraLectiva[][] setmana) {
        this.setmana = setmana;
    }

    public void setHoraLectiva(HoraLectiva hL, int dia, int hora) {
        if (hora < 0 || hora >= this.hores || dia < 0 || dia >= 5) {
            System.out.println(">>> getHoraLectiva(): Parametres hora i dia no valids.");
        }
        else this.setmana[dia][hora] = hL;
    }

    public void setHores(int hores) {
        this.hores = hores;
    }

    public void setHIni(int hIni) {
        this.hIni = hIni;
    }

    private void iniSetmana() {
        for (int i = 0; i < dies; i++) {
            for (int j = 0; j < hores; j++) {
                this.setmana[i][j] = new HoraLectiva();
            }
        }
    }
}