package com.example.chat.Notifications;

public class Data {
    private  String user;
    private int icon;
    private String msj;
    private String namerecivier;
    private String title;
    private String sented;
    private String dato;

    public Data(String user, int icon, String msj, String namerecivier, String title, String sented, String dato) {
        this.user = user;
        this.icon = icon;
        this.msj = msj;
        this.namerecivier = namerecivier;
        this.title = title;
        this.sented = sented;
        this.dato = dato;
    }

    public Data() {
    }

    public String getDato() {
        return dato;
    }

    public void setDato(String dato) {
        this.dato = dato;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getMsj() {
        return msj;
    }

    public void setMsj(String msj) {
        this.msj = msj;
    }

    public String getNamerecivier() {
        return namerecivier;
    }

    public void setNamerecivier(String namerecivier) {
        this.namerecivier = namerecivier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
    }
}
