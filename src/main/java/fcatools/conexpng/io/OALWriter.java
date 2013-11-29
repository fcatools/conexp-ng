package fcatools.conexpng.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.Conf;

public class OALWriter {
    public OALWriter(Conf state, String path) throws IOException {

        FileOutputStream fos = new FileOutputStream(path);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (FullObject<String, String> obj : state.context.getObjects()) {
            bw.append(obj.getIdentifier() + ":");
            int i = 0;
            for (String attr : obj.getDescription().getAttributes()) {
                bw.append(attr);
                i++;
                if (i < obj.getDescription().getAttributes().size())
                    bw.append(";");
            }
            bw.newLine();
        }

        bw.close();
        fos.close();
    }
}
