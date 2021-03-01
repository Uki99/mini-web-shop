package main;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import models.Kategorije;
import models.Proizvodi;

import java.io.*;
import java.util.ArrayList;

public class Data {
    public static boolean writeToJSONProizvodi(ArrayList<Proizvodi> list, String path){
        try {

            // Ova petlja sluzi da skinemo nazive koji nisu postojali u originalnom JSON-u pre upisa
            for(Proizvodi k: list) {
                k.setKategorija(null);
            }

            Writer writer=new FileWriter(path);
            Gson gson = new Gson();
            gson.toJson(list,writer);
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeToJSONKategorije(ArrayList<Kategorije> list, String path){
        try {
            Writer writer=new FileWriter(path);
            Gson gson = new Gson();
            gson.toJson(list,writer);
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<Proizvodi> readFromJsonProizvodi(String path){
        if(!new File(path).exists()){
            return new ArrayList<>();
        }
        try {
            JsonReader reader=new JsonReader(new FileReader(path));
            Gson gson = new Gson();

            return gson.fromJson(reader,new TypeToken<ArrayList<Proizvodi>>(){}.getType());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static ArrayList<Kategorije> readFromJsonKategorije(String path){
        if(!new File(path).exists()){
            return new ArrayList<>();
        }
        try {
            JsonReader reader=new JsonReader(new FileReader(path));
            Gson gson = new Gson();

            return gson.fromJson(reader,new TypeToken<ArrayList<Kategorije>>(){}.getType());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}