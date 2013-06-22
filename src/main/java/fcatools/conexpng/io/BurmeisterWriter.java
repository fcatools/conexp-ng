package fcatools.conexpng.io;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import de.tudresden.inf.tcs.fcalib.FullObject;

import fcatools.conexpng.ProgramState;

public class BurmeisterWriter {

    private final String EOL = System.getProperty("line.separator");

    public BurmeisterWriter(ProgramState state) {
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(state.filePath);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.append("B" + EOL);
            bw.append(state.context.getObjectCount() + EOL);
            bw.append(state.context.getAttributeCount() + EOL);
            for (FullObject<String, String> obj : state.context.getObjects()) {
                bw.append(obj.getIdentifier() + EOL);
            }
            for (String attr : state.context.getAttributes()) {
                bw.append(attr + EOL);
            }
            for (FullObject<String, String> obj : state.context.getObjects()) {
                for (String attr : state.context.getAttributes()) {
                    bw.append(state.context.objectHasAttribute(obj, attr) ? "X"
                            : ".");
                }
                bw.append(EOL);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                bw.close();
                fos.close();
            } catch (Exception ex) {
            }
        }
    }
}
