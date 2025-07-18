package com.cinemamanager.util;
import com.google.gson.Gson;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public final class JsonUtil <T, K, V, C extends Collection <T>, M extends Map <K, V> > {

    private static final Gson GSON = new Gson ();

    public static <T> void write (String path,
                                  Collection <T> collection)
    {
        try (FileWriter writer = new FileWriter(path))
        {
            GSON.toJson(collection, writer);
            System.out.println("Data has been successfully saved to file: " + path + "!\n");
        }
        catch (IOException e)
        {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public static <K, V> void write (String path,
                                     Map <K, V> map)
    {
        try (FileWriter writer = new FileWriter (path)) {
            GSON.toJson(map, writer);
            System.out.println("Data has been successfully saved to file: " + path + "!\n");
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    public static <T, C extends Collection <T> > C readCollection (String path,
                                                         Type typeCollection,
                                                         Supplier <C> collectionSupplier)
    {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return collectionSupplier.get();
        }
        try (FileReader reader = new FileReader(path)) {
            return GSON.fromJson(reader, typeCollection);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return collectionSupplier.get();
        }
    }

    public static <K, V, M extends Map <K, V> > Map <K, V> readMap (String path,
                                                                    Type typeMap,
                                                                    Supplier <M> mapSupplier)
    {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return mapSupplier.get();
        }
        try (FileReader reader = new FileReader(path)) {
            return GSON.fromJson(reader, typeMap);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return mapSupplier.get();
        }
    }

}
