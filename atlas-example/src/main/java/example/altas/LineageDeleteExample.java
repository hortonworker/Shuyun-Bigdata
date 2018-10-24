package example.altas;

import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.atlas.*;
import org.apache.atlas.model.SearchFilter;
import org.apache.atlas.model.typedef.AtlasTypesDef;
import org.apache.atlas.utils.AuthenticationUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

import static org.apache.atlas.model.instance.AtlasEntity.AtlasEntityExtInfo;
import static org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;


/**
 * A driver that sets up sample types and entities using v2 types and entity model for testing purposes.
 */
public class LineageDeleteExample {
    public static final String ATLAS_REST_ADDRESS = "atlas.rest.address";


    //database
    public static final String EXAMPLE_DB = "Example_DB";

    //tables
    public static final String LOG_DAILY_MV = "log_daily_example_mv";
    public static final String LOG_MONTHLY_MV = "log_monthly_example_mv";

    //columns
    public static final String TIME_ID_DATE_COLUMN = "time_date_id";
    public static final String TIME_ID_MONTH_COLUMN = "time_month_id";
    public static final String DAILY_MILE_COLUMN = "daily_mile";
    public static final String DAILY_OIL_CONSUMPTION_COLUMN = "daily_oil_consumption";
    public static final String VEHICLE_IDENTIFY_NUMBER_MONTH_COLUMN = "vin_month";
    public static final String VEHICLE_IDENTIFY_NUMBER_DATE_COLUMN = "vin_date";
    public static final String MONTHLY_MILE_COLUMN = "monthly_mile";
    public static final String MONTHLY_OIL_CONSUMPTION_COLUMN = "monthly_oil_consumption";


    //classification
    public static final String ETL_CLASSIFICATION = "ETL";
    public static final String LOGDATA_CLASSIFICATION = "Log Data";

    //processes
    public static final String AGGREGATION_PROCESS = "aggregation_from_daily_to_monthly";

    //types
    public static final String DATABASE_TYPE = "DB";
    public static final String COLUMN_TYPE = "Column";
    public static final String TABLE_TYPE = "Table";
    public static final String LOAD_PROCESS_TYPE = "LoadProcess";

    public static final String[] TYPES = {LOAD_PROCESS_TYPE, DATABASE_TYPE, TABLE_TYPE, TABLE_TYPE, COLUMN_TYPE,
            ETL_CLASSIFICATION, LOGDATA_CLASSIFICATION};

    public static final String[] COLUMN_TYPES = {TIME_ID_DATE_COLUMN, TIME_ID_MONTH_COLUMN, DAILY_MILE_COLUMN, DAILY_OIL_CONSUMPTION_COLUMN,
            VEHICLE_IDENTIFY_NUMBER_MONTH_COLUMN, VEHICLE_IDENTIFY_NUMBER_DATE_COLUMN, MONTHLY_MILE_COLUMN, MONTHLY_OIL_CONSUMPTION_COLUMN};

    private final AtlasClientV2 atlasClientV2;

    LineageDeleteExample(String[] urls, String[] basicAuthUsernamePassword) {
        atlasClientV2 = new AtlasClientV2(urls, basicAuthUsernamePassword);
    }

    LineageDeleteExample(String[] urls) throws AtlasException {
        atlasClientV2 = new AtlasClientV2(urls);
    }

    public static void main(String[] args) throws Exception {
        String[] basicAuthUsernamePassword = null;

        if (!AuthenticationUtil.isKerberosAuthenticationEnabled()) {
            basicAuthUsernamePassword = AuthenticationUtil.getBasicAuthenticationInput();
        }

        runQuickstart(args, basicAuthUsernamePassword);
    }

    @VisibleForTesting
    static void runQuickstart(String[] args, String[] basicAuthUsernamePassword) throws Exception {
        String[] urls = getServerUrl(args);
        LineageDeleteExample lineage;

        if (!AuthenticationUtil.isKerberosAuthenticationEnabled()) {
            lineage = new LineageDeleteExample(urls, basicAuthUsernamePassword);
        } else {
            lineage = new LineageDeleteExample(urls);
        }


        // Shows how to create v2 entities (instances) for the added types in Atlas
        lineage.clearentities();

    }

    static String[] getServerUrl(String[] args) throws AtlasException {
        if (args.length > 0) {
            return args[0].split(",");
        }

        Configuration configuration = ApplicationProperties.get();
        String[] urls = configuration.getStringArray(ATLAS_REST_ADDRESS);

        if (ArrayUtils.isEmpty(urls)) {
            System.out.println("org.apache.atlas.examples.QuickStartV2 <Atlas REST address <http/https>://<atlas-fqdn>:<atlas-port> like http://localhost:21000>");
            System.exit(-1);
        }

        return urls;
    }

    void clearentities() throws Exception {
        System.out.println("\nDeleting sample entities: ");
        String uid = null;


        //delete columns
        for (String column_type : COLUMN_TYPES) {
            atlasClientV2.deleteEntityByGuid(getEntityGuid(column_type, COLUMN_TYPE));
        }

        //delete process
        atlasClientV2.deleteEntityByGuid(getEntityGuid(AGGREGATION_PROCESS, LOAD_PROCESS_TYPE));


        //delete tables
        atlasClientV2.deleteEntityByGuid(getEntityGuid(LOG_DAILY_MV, TABLE_TYPE));
        atlasClientV2.deleteEntityByGuid(getEntityGuid(LOG_MONTHLY_MV, TABLE_TYPE));

        //delete database
        atlasClientV2.deleteEntityByGuid(getEntityGuid(EXAMPLE_DB, DATABASE_TYPE));

        //delete classification & type
        MultivaluedMap<String, String> searchParams = new MultivaluedMapImpl();

        for (String typeName : TYPES) {
            searchParams.clear();
            searchParams.add(SearchFilter.PARAM_NAME, typeName);
            SearchFilter searchFilter = new SearchFilter(searchParams);

            AtlasTypesDef searchDefs = atlasClientV2.getAllTypeDefs(searchFilter);
            atlasClientV2.deleteAtlasTypeDefs(searchDefs);
        }


//        Map<String, String> attributes = new HashMap<>();
//        attributes.put(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME, LOG_DAILY_MV);
//
//        AtlasEntity entity = atlasClientV2.getEntityByAttribute(TABLE_TYPE, attributes).getEntity();
//
//        Map<String, Object> hello = entity.getAttributes();
//
//        System.out.println(hello.keySet().toString());
    }


    private String getEntityGuid(String name, String type) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME, name);

        AtlasEntityExtInfo entityExtInfo = null;
        try {
            entityExtInfo = atlasClientV2.getEntityByAttribute(type, attributes);
        } catch (AtlasServiceException e) {
            e.printStackTrace();
        }

        String guid = ((AtlasEntityWithExtInfo) entityExtInfo).getEntity().getGuid();
        System.out.println(guid);
        return guid;
    }
}