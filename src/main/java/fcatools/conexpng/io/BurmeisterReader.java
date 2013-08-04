package fcatools.conexpng.io;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.Conf;
import fcatools.conexpng.model.FormalContext;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class BurmeisterReader {

    public BurmeisterReader(Conf state, String path) throws IllegalObjectException, IOException {
        FileInputStream fis = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;

        while ((line = br.readLine()).trim().equals("")) {
        }
        FormalContext context = new FormalContext();
        if (line.equals("B")) {
            // if there is a free line
            if ((line = br.readLine()).trim().equals("")) {
                line = br.readLine();
            }
            int objCount = 0;
            try {
                objCount = Integer.parseInt(line);
            } catch (NumberFormatException ex) {
                line = br.readLine();
                objCount = Integer.parseInt(line);
            }
            int attrCount = Integer.parseInt(br.readLine());

            if ((line = br.readLine()).trim().equals("")) {
                line = br.readLine();
            }
            for (int i = 0; i < objCount; i++) {

                context.addObject(new FullObject<String, String>(line));
                line = br.readLine();

            }
            while (line.trim().equals("")) {
                line = br.readLine();
            }
            for (int i = 0; i < attrCount; i++) {
                context.addAttribute(line);
                line = br.readLine();
            }
            while (line.trim().equals("")) {
                line = br.readLine();
            }
            for (int i = 0; i < objCount; i++) {
                int j = 0;
                FullObject<String, String> obj = context.getObjectAtIndex(i);
                if (!(line.contains("X") || line.contains("x")) && !line.contains(".")) {
                    br.close();
                    throw new IOException("Attribute/Object count was wrong");
                }
                for (char c : line.toCharArray()) {
                    if (c != '.')

                        context.addAttributeToObject(context.getAttributeAtIndex(j), obj.getIdentifier());

                    j++;
                }
                line = br.readLine();
            }
            br.close();
        } else {
            br.close();
            throw new IOException("file starts not with a \"B\"");
        }

        state.guiConf.columnWidths = new HashMap<>();

        path = path.substring(0, path.lastIndexOf(System.getProperty("file.separator")) + 1) + "untitled.cex";

        state.setNewFile(path);
        state.newContext(context);
    }
}
