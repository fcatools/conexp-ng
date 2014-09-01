package fcatools.conexpng.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.Conf;
import fcatools.conexpng.model.FormalContext;

public class CSVReader {

    public CSVReader(Conf state, String path) throws IllegalObjectException, IOException {
        FileInputStream fis = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        FormalContext context = new FormalContext();

        line = br.readLine();
        String[] attr = line.split(";");
        for (int i = 1; i < attr.length; i++) {
            context.addAttribute(attr[i]);
        }
        while ((line = br.readLine()) != null) {
            String[] obj = line.split(";");
            Set<String> attrForObj = new TreeSet<>();
            for (int i = 1; i < obj.length; i++) {
                if (obj[i].equals("1"))
                    attrForObj.add(context.getAttributeAtIndex(i - 1));
            }
            context.addObject(new FullObject<String, String>(obj[0], attrForObj));
        }
        br.close();

        state.setNewFile(path);
        state.newContext(context);
    }
}
