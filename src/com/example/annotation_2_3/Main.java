package com.example.annotation_2_3;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;

class FileOperation{
    @Save
    private String login;
    @Save
    private int id;


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}

class Serialize{
    public static String serialize(Object o,String path) throws IllegalAccessException {
        Class <?> cls=o.getClass();
        StringBuilder sb=new StringBuilder();
        Field[] fields=cls.getDeclaredFields();
        for(Field field: fields){
            if(field.isAnnotationPresent(Save.class)){
                if(Modifier.isPrivate(field.getModifiers())){
                    field.setAccessible(true);
                }
                sb.append(field.getName()).append("=");
                sb.append(field.get(o));
                sb.append(";");
            }
        }

        try(FileWriter w=new FileWriter(path)) {
            w.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static <T> T deserialize( Class<T> cls,String path) throws IOException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        T result= (T)cls.newInstance();
        FileReader r=new FileReader(path);
        char [] a = new char[200];
        r.read(a);
        r.close();
        String text;
        text=String.valueOf(a);
        text=text.trim();
        String[] pairs=text.split(";");

        for(String p:pairs){
            String[] nv=p.split("=");
            if(nv.length!=2)
                throw new InvalidParameterException();

            String name=nv[0];
            String value=nv[1];
            Field f=cls.getDeclaredField(name);
            if(Modifier.isPrivate(f.getModifiers())){
                f.setAccessible(true);
            }
            if(f.isAnnotationPresent(Save.class)){
                if(f.getType()==int.class)
                    f.set(result,Integer.parseInt(value));
                else if(f.getType()==String.class)
                    f.set(result,value);
            }
        }
        return result;
    }
}

public class Main {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, IOException, NoSuchFieldException{
        FileOperation fileOperation=new FileOperation();
        fileOperation.setLogin("Vladomer87");
        fileOperation.setId(12345);
        String path="file.txt";
        String serializeResult=Serialize.serialize(fileOperation,path);
        System.out.println("Serialized: "+serializeResult);
        fileOperation=Serialize.deserialize(FileOperation.class,path);
        System.out.println("Deserialized: "+fileOperation.getLogin()+
                ","+fileOperation.getId());
    }
}
