package me.alpha432.oyvey.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Objects;

public class ReflectionUtilPhob {
    public static
    < F, T extends F > void copyOf ( F from , T to , boolean ignoreFinal ) throws NoSuchFieldException, IllegalAccessException {
        Objects.requireNonNull ( from );
        Objects.requireNonNull ( to );
        Class < ? > clazz = from.getClass ( );
        for (Field field : clazz.getDeclaredFields ( )) {
            ReflectionUtilPhob.makePublic ( field );
            if ( ReflectionUtilPhob.isStatic ( field ) || ignoreFinal && ReflectionUtilPhob.isFinal ( field ) ) continue;
            ReflectionUtilPhob.makeMutable ( field );
            field.set ( to , field.get ( from ) );
        }
    }

    public static
    < F, T extends F > void copyOf ( F from , T to ) throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtilPhob.copyOf ( from , to , false );
    }

    public static
    boolean isStatic ( Member instance ) {
        return ( instance.getModifiers ( ) & 8 ) != 0;
    }

    public static
    boolean isFinal ( Member instance ) {
        return ( instance.getModifiers ( ) & 0x10 ) != 0;
    }

    public static
    void makeAccessible ( AccessibleObject instance , boolean accessible ) {
        Objects.requireNonNull ( instance );
        instance.setAccessible ( accessible );
    }

    public static
    void makePublic ( AccessibleObject instance ) {
        ReflectionUtilPhob.makeAccessible ( instance , true );
    }

    public static
    void makePrivate ( AccessibleObject instance ) {
        ReflectionUtilPhob.makeAccessible ( instance , false );
    }

    public static
    void makeMutable ( Member instance ) throws NoSuchFieldException, IllegalAccessException {
        Objects.requireNonNull ( instance );
        Field modifiers = Field.class.getDeclaredField ( "modifiers" );
        ReflectionUtilPhob.makePublic ( modifiers );
        modifiers.setInt ( instance , instance.getModifiers ( ) & 0xFFFFFFEF );
    }

    public static
    void makeImmutable ( Member instance ) throws NoSuchFieldException, IllegalAccessException {
        Objects.requireNonNull ( instance );
        Field modifiers = Field.class.getDeclaredField ( "modifiers" );
        ReflectionUtilPhob.makePublic ( modifiers );
        modifiers.setInt ( instance , instance.getModifiers ( ) & 0x10 );
    }
}
