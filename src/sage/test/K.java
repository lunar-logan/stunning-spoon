package sage.test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Dell on 03-06-2016.
 */
public class K {
    public static void main(String[] args) throws URISyntaxException {
        URI uri = new URI("p:u/%E2%80%A2");
        System.out.println(uri);
        System.out.println(uri.isAbsolute());
    }
}
