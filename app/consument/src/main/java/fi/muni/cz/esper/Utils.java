package fi.muni.cz.esper;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

/**
 * Created by tomasskopal on 24.10.15.
 */
public class Utils {

    public static EPServiceProvider getServiceProvider() {
        Configuration cepConfig = new Configuration();
        cepConfig.addEventType(IncommingEvent.class);
        EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
        return cep;
    }
}
