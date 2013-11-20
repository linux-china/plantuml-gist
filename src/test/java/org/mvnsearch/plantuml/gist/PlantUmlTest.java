package org.mvnsearch.plantuml.gist;

import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * plantuml test
 *
 * @author linux_china
 */
public class PlantUmlTest {

    @Test
    public void testRender() throws Exception {
        String source = IOUtils.toString(this.getClass().getResourceAsStream("/demo-sequence.puml"));
        OutputStream output = new ByteArrayOutputStream();
        SourceStringReader reader = new SourceStringReader(source);
        String desc = reader.generateImage(output);
        System.out.println(desc);
    }
}
