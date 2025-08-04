package ec.gob.funcionjudicial.utils;

import ec.gob.funcionjudicial.exception.ConsejoJudicaturaException;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IdentidadUtils {

    private static IdentidadUtils instance;

    private IdentidadUtils(){
        //sin argumentos
    }

    public static IdentidadUtils getInstance(){
        if(instance == null){
            instance = new IdentidadUtils();
        }
        return instance;
    }

    public String codificarObjeto(Object object) {

        if (object == null) {
            return null;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(object);
            oos.flush();

            return Base64.encodeBase64String(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error al codificar usuario", e);
        }

    }

    public <T> T decodificarObjeto(String objectoCodificado) {

        if (objectoCodificado == null || objectoCodificado.isEmpty()) {
            return null;
        }

        try {

            byte[] data = Base64.decodeBase64(objectoCodificado);

            try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais)) {
                Object outcome = ois.readObject();
                return (T) outcome;
            }

        } catch (IOException | ClassNotFoundException e){
            throw new ConsejoJudicaturaException("Error al decodificar usuario", e);
        }

    }
}
