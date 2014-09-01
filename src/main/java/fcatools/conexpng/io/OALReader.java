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

public class OALReader {

    public OALReader(Conf state, String path) throws IllegalObjectException, IOException {
        FileInputStream fis = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        FormalContext context = new FormalContext();

        while ((line = br.readLine()) != null) {
            String[] attr = line.substring(line.indexOf(":") + 1, line.length()).split(";");
            Set<String> attrs = new TreeSet<>();
            for (String string : attr) {
                attrs.add(string);
                if (!context.getAttributes().contains(string))
                    context.addAttribute(string);
            }
            context.addObject(new FullObject<String, String>(line.substring(0, line.indexOf(":")), attrs));
        }
        br.close();

        state.setNewFile(path);
        state.newContext(context);
    }
}
