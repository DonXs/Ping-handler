package de.donxs.pinghandler;

import de.donxs.pinghandler.callback.Callback;
import java.util.Arrays;
import java.util.Collection;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class PingHandlerTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][]{{"gommehd.net", 25565}}
        );
    }

    @Parameter(0)
    public String address;
    @Parameter(1)
    public int port;

    private final PingHandler handler = new PingHandler();

    @Test
    public void fetch() throws Exception {

        assertThat(handler, notNullValue());

        handler.fetch(this.address, this.port, new Callback<PingResponse>() {

            @Override
            public void done(PingResponse value, Throwable throwable) {

                assertThat(value, notNullValue());
                assertThat(value.getPlainjson(), not(emptyOrNullString()));
                System.out.println(value.getVersion().getName());
                System.out.println(value.getDescription());

                synchronized (PingHandlerTest.this) {
                    PingHandlerTest.this.notify();
                }

            }

        });

        synchronized (this) {
            wait();
        }

    }

    @After
    public void exit() throws Exception {

        this.handler.shutdown();

    }

}
