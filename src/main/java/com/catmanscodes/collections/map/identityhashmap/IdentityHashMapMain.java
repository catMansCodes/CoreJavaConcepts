package com.catmanscodes.collections.map.identityhashmap;

import java.util.IdentityHashMap;

public class IdentityHashMapMain {
    public static void main(String[] args) {

        IdentityHashMap<String, Integer> map = new IdentityHashMap<>();

        String key1 = new String("key");
        String key2 = new String("key");

        map.put(key1, 12);
        map.put(key2, 11);

        //In IdentityHashMap it use it own hashcode & == and allowed the keys
        // Bz new keyword create new memory(Obj heap) each time and == just check both are diff obj
        // it don't use equals() method internally & just compare the memory address only using ==

        System.out.println(key1.hashCode() + " : " + key2.hashCode());//both are same
        System.out.println(map); //{key=12, key=11}

    }
}
