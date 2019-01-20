package com.accesscontrol.hephaestus.ameeting;

public class Meeting {
    private int id;
    private String name;
    private String time;

    public Meeting(int id,String name,String time){
        super();
        this.id=id;
        this.name=name;
        this.time=time;
    }
    public Meeting(){
        super();
    }
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id=id;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }

    public String getTime(){
        return time;
    }
    public void setTime(String time){
        this.time=time;
    }

    @Override
    public String toString() {
        return "\n"+"id="+getId()+
                "name="+getName()+
                "time="+getTime()+"\n";
    }
}
