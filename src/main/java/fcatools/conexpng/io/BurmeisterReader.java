package fcatools.conexpng.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import de.tudresden.inf.tcs.fcaapi.exception.IllegalAttributeException;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.ProgramState;
import fcatools.conexpng.model.FormalContext;

public class BurmeisterReader {

    public BurmeisterReader(ProgramState state) {
        try {
            FileInputStream fis = new FileInputStream(state.filePath);
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
                    try {
                        context.addObject(new FullObject<String, String>(line));
                        line = br.readLine();
                    } catch (IllegalObjectException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
                    FullObject<String, String> obj = context
                            .getObjectAtIndex(i);
                    for (char c : line.toCharArray()) {
                        if (c != '.')
                            try {
                                context.addAttributeToObject(
                                        context.getAttributeAtIndex(j),
                                        obj.getIdentifier());
                            } catch (IllegalAttributeException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IllegalObjectException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        j++;
                    }
                    line = br.readLine();
                }
            }

            br.close();
            state.newContext(context);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
