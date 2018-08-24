package example.altas;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.atlas.*;
import org.apache.atlas.model.SearchFilter;
import org.apache.atlas.model.instance.*;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.typedef.*;
import org.apache.atlas.type.AtlasTypeUtil;
import org.apache.atlas.utils.AuthenticationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

import static org.apache.atlas.model.typedef.AtlasStructDef.AtlasConstraintDef.CONSTRAINT_PARAM_ATTRIBUTE;
import static org.apache.atlas.model.typedef.AtlasStructDef.AtlasConstraintDef.CONSTRAINT_TYPE_INVERSE_REF;
import static org.apache.atlas.model.typedef.AtlasStructDef.AtlasConstraintDef.CONSTRAINT_TYPE_OWNED_REF;

/**
 * A driver that sets up sample types and entities using v2 types and entity model for testing purposes.
 */
public class LineageAddExample {
    public static final String ATLAS_REST_ADDRESS = "atlas.rest.address";

    //database
    public static final String EXAMPLE_DB= "Example_DB";

    //tables
    public static final String LOG_DAILY_MV = "log_daily_example_mv";
    public static final String LOG_MONTHLY_MV = "log_monthly_example_mv";

    //columns
    public static final String TIME_ID_DATE_COLUMN               = "time_date_id";
    public static final String TIME_ID_MONTH_COLUMN               = "time_month_id";
    public static final String DAILY_MILE_COLUMN                 = "daily_mile";
    public static final String DAILY_OIL_CONSUMPTION_COLUMN      = "daily_oil_consumption";
    public static final String VEHICLE_IDENTIFY_NUMBER_MONTH_COLUMN    = "vin_month";
    public static final String VEHICLE_IDENTIFY_NUMBER_DATE_COLUMN    = "vin_date";
    public static final String MONTHLY_MILE_COLUMN               = "monthly_mile";
    public static final String MONTHLY_OIL_CONSUMPTION_COLUMN    = "monthly_oil_consumption";


    //classification
    public static final String ETL_CLASSIFICATION = "ETL";
    public static final String LOGDATA_CLASSIFICATION = "Log Data";

    //processes
    public static final String AGGREGATION_PROCESS = "aggregation_from_daily_to_monthly";

    //types
    public static final String DATABASE_TYPE               = "DB";
    public static final String COLUMN_TYPE                 = "Column";
    public static final String TABLE_TYPE                  = "Table";
    public static final String LOAD_PROCESS_TYPE           = "LoadProcess";

    public static final String[] TYPES = { DATABASE_TYPE, TABLE_TYPE, COLUMN_TYPE, LOAD_PROCESS_TYPE,
            ETL_CLASSIFICATION, LOGDATA_CLASSIFICATION };

    private final AtlasClientV2 atlasClientV2;

    LineageAddExample(String[] urls, String[] basicAuthUsernamePassword) {
        atlasClientV2 = new AtlasClientV2(urls, basicAuthUsernamePassword);
    }

    LineageAddExample(String[] urls) throws AtlasException {
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
        LineageAddExample lineage;

        if (!AuthenticationUtil.isKerberosAuthenticationEnabled()) {
            lineage = new LineageAddExample(urls, basicAuthUsernamePassword);
        } else {
            lineage = new LineageAddExample(urls);
        }


        // Shows how to create v2 types in Atlas for your meta model
        lineage.createTypes();

        // Shows how to create v2 entities (instances) for the added types in Atlas
        lineage.createEntities();


        // Shows some lineage information on entity
        lineage.lineage();

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


    void createTypes() throws Exception {
        AtlasTypesDef atlasTypesDef = createTypeDefinitions();

        System.out.println("\nCreating sample types: ");
        atlasClientV2.createAtlasTypeDefs(atlasTypesDef);

        verifyTypesCreated();
    }

    private void verifyTypesCreated() throws Exception {
        MultivaluedMap<String, String> searchParams = new MultivaluedMapImpl();

        for (String typeName : TYPES) {
            searchParams.clear();
            searchParams.add(SearchFilter.PARAM_NAME, typeName);
            SearchFilter searchFilter = new SearchFilter(searchParams);
            AtlasTypesDef searchDefs = atlasClientV2.getAllTypeDefs(searchFilter);

            //just prove not empty for each type
            assert (!searchDefs.isEmpty());
            System.out.println("Created type [" + typeName + "]");
        }
    }

    AtlasTypesDef createTypeDefinitions() throws Exception {

        List<AtlasClassificationDef> classificationList = new LinkedList<AtlasClassificationDef>();
        List<AtlasEntityDef> typeList = new LinkedList<AtlasEntityDef>();


        if(atlasClientV2.typeWithNameExists(DATABASE_TYPE)==false){

            AtlasEntityDef dbType   = AtlasTypeUtil.createClassTypeDef(DATABASE_TYPE, DATABASE_TYPE, "1.0", ImmutableSet.of("DataSet"),
                    AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("description", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("locationUri", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("owner", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("createTime", "long"));

            typeList.add(dbType);

        }

        if(atlasClientV2.typeWithNameExists(COLUMN_TYPE)==false){

            AtlasEntityDef colType  = AtlasTypeUtil.createClassTypeDef(COLUMN_TYPE, COLUMN_TYPE, "1.0", ImmutableSet.of("DataSet"),
                    AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("dataType", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("comment", "string"),
                    AtlasTypeUtil.createOptionalAttrDefWithConstraint("table", TABLE_TYPE, CONSTRAINT_TYPE_INVERSE_REF,
                            new HashMap<String, Object>() {{ put(CONSTRAINT_PARAM_ATTRIBUTE, "columns"); }}));

            colType.setOptions(new HashMap<String, String>() {{ put("schemaAttributes", "[\"description\", \"owner\", \"type\", \"comment\", \"position\"]"); }});

            typeList.add(colType);
        }

        if(atlasClientV2.typeWithNameExists(TABLE_TYPE)==false){

            AtlasEntityDef tblType  = AtlasTypeUtil.createClassTypeDef(TABLE_TYPE, TABLE_TYPE, "1.0", ImmutableSet.of("DataSet"),
                    AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                    AtlasTypeUtil.createRequiredAttrDef("db", DATABASE_TYPE),
                    AtlasTypeUtil.createOptionalAttrDef("owner", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("createTime", "long"),
                    AtlasTypeUtil.createOptionalAttrDef("lastAccessTime", "long"),
                    AtlasTypeUtil.createOptionalAttrDef("retention", "long"),
                    AtlasTypeUtil.createOptionalAttrDef("viewOriginalText", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("viewExpandedText", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("tableType", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("temporary", "boolean"),
                    AtlasTypeUtil.createRequiredListAttrDefWithConstraint("columns", AtlasBaseTypeDef.getArrayTypeName(COLUMN_TYPE),
                            CONSTRAINT_TYPE_OWNED_REF, null));

            tblType.setOptions(new HashMap<String, String>() {{ put("schemaElementsAttribute", "columns"); }});

            typeList.add(tblType);
        }

        if(atlasClientV2.typeWithNameExists(LOAD_PROCESS_TYPE)==false){

            AtlasEntityDef procType = AtlasTypeUtil.createClassTypeDef(LOAD_PROCESS_TYPE, LOAD_PROCESS_TYPE, "1.0", ImmutableSet.of("Process"),
                    AtlasTypeUtil.createUniqueRequiredAttrDef("name", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("userName", "string"),
                    AtlasTypeUtil.createOptionalAttrDef("startTime", "long"),
                    AtlasTypeUtil.createOptionalAttrDef("endTime", "long"),
                    AtlasTypeUtil.createRequiredAttrDef("queryText", "string"),
                    AtlasTypeUtil.createRequiredAttrDef("queryPlan", "string"),
                    AtlasTypeUtil.createRequiredAttrDef("queryId", "string"),
                    AtlasTypeUtil.createRequiredAttrDef("queryGraph", "string"));

            typeList.add(procType);
        }

        //tags definition

        if(atlasClientV2.typeWithNameExists(ETL_CLASSIFICATION)==false){
            AtlasClassificationDef etlClassifDef    = AtlasTypeUtil.createTraitTypeDef(ETL_CLASSIFICATION, "ETL Classification", "1.0", ImmutableSet.<String>of());

            classificationList.add(etlClassifDef);
        }


        if(atlasClientV2.typeWithNameExists(LOGDATA_CLASSIFICATION)==false){
            AtlasClassificationDef logClassifDef    = AtlasTypeUtil.createTraitTypeDef(LOGDATA_CLASSIFICATION, "LogData Classification", "1.0", ImmutableSet.<String>of());

            classificationList.add(logClassifDef);
        }


        return AtlasTypeUtil.getTypesDef(ImmutableList.<AtlasEnumDef>of(),
                ImmutableList.<AtlasStructDef>of(),
                classificationList,
                typeList);
    }

    void createEntities() throws Exception {
        System.out.println("\nCreating sample entities: ");

        // Database entities
        AtlasEntity logDB     = createDatabase(EXAMPLE_DB, "sales database", "John ETL", "hdfs://host:8000/apps/warehouse/example");

        // Column entities
        List<AtlasEntity> logColumns = ImmutableList.of(
                createColumn(TIME_ID_DATE_COLUMN, "int","time id"),
                createColumn(VEHICLE_IDENTIFY_NUMBER_DATE_COLUMN, "int","app id"),
                createColumn(DAILY_MILE_COLUMN, "int", "machine id", LOGDATA_CLASSIFICATION),
                createColumn(DAILY_OIL_CONSUMPTION_COLUMN, "string", "log data", LOGDATA_CLASSIFICATION));

        List<AtlasEntity> salesFactColumns   = ImmutableList.of(
                createColumn(TIME_ID_MONTH_COLUMN, "int", "time id"),
                createColumn(VEHICLE_IDENTIFY_NUMBER_MONTH_COLUMN, "int", "product id"),
                createColumn(MONTHLY_MILE_COLUMN, "int", "customer id", LOGDATA_CLASSIFICATION),
                createColumn(MONTHLY_OIL_CONSUMPTION_COLUMN, "double", "product id", LOGDATA_CLASSIFICATION));

        // Table entities
        AtlasEntity loggingDaily = createTable(LOG_DAILY_MV, "example log daily collect", logDB,
                "Tim ETL", "Managed", logColumns, LOGDATA_CLASSIFICATION);
        AtlasEntity loggingMonthly = createTable(LOG_MONTHLY_MV, "example log monthly collect", logDB,"Tim ETL", "Managed", salesFactColumns, LOGDATA_CLASSIFICATION);


        // Process entities
        createProcess(AGGREGATION_PROCESS, "hive query for daily summary", "John ETL",
                ImmutableList.of(loggingDaily),
                ImmutableList.of(loggingMonthly),
                "create table as select ", "plan", "id", "graph", ETL_CLASSIFICATION);
    }

    AtlasEntity createDatabase(String name, String description, String owner, String locationUri, String... traitNames)
            throws Exception {
        AtlasEntity entity = new AtlasEntity(DATABASE_TYPE);
        entity.setAttribute(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME, name);
        entity.setClassifications(toAtlasClassifications(traitNames));
        entity.setAttribute("name", name);
        entity.setAttribute("description", description);
        entity.setAttribute("owner", owner);
        entity.setAttribute("locationuri", locationUri);
        entity.setAttribute("createTime", System.currentTimeMillis());

        return createInstance(entity, traitNames);
    }

    private AtlasEntity createInstance(AtlasEntity entity, String[] traitNames) throws Exception {
        AtlasEntity ret = null;
        EntityMutationResponse response = atlasClientV2.createEntity(new AtlasEntity.AtlasEntityWithExtInfo(entity));
        List<AtlasEntityHeader> entities = response.getEntitiesByOperation(EntityMutations.EntityOperation.CREATE);

        if (CollectionUtils.isNotEmpty(entities)) {
            AtlasEntity.AtlasEntityWithExtInfo getByGuidResponse = atlasClientV2.getEntityByGuid(entities.get(0).getGuid());
            ret = getByGuidResponse.getEntity();
            System.out.println("Created entity of type [" + ret.getTypeName() + "], guid: " + ret.getGuid()+", name: "+ret.getAttribute("name"));
        }

        return ret;
    }


    private List<AtlasClassification> toAtlasClassifications(String[] traitNames) {
        List<AtlasClassification> ret = new ArrayList<>();
        ImmutableList<String> traits = ImmutableList.copyOf(traitNames);

        if (CollectionUtils.isNotEmpty(traits)) {
            for (String trait : traits) {
                ret.add(new AtlasClassification(trait));
            }
        }

        return ret;
    }


    AtlasEntity createColumn(String name, String dataType, String comment, String... traitNames) throws Exception {

        AtlasEntity entity = new AtlasEntity(COLUMN_TYPE);
        entity.setClassifications(toAtlasClassifications(traitNames));
        entity.setAttribute(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME, name);
        entity.setAttribute("name", name);
        entity.setAttribute("dataType", dataType);
        entity.setAttribute("comment", comment);

        return createInstance(entity, traitNames);
    }

    AtlasEntity createTable(String name, String description, AtlasEntity db, String owner, String tableType,
                            List<AtlasEntity> columns, String... traitNames) throws Exception {
        AtlasEntity entity = new AtlasEntity(TABLE_TYPE);

        entity.setClassifications(toAtlasClassifications(traitNames));
        entity.setAttribute("name", name);
        entity.setAttribute(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME, name);
        entity.setAttribute("description", description);
        entity.setAttribute("owner", owner);
        entity.setAttribute("tableType", tableType);
        entity.setAttribute("createTime", System.currentTimeMillis());
        entity.setAttribute("lastAccessTime", System.currentTimeMillis());
        entity.setAttribute("retention", System.currentTimeMillis());
        entity.setAttribute("db", AtlasTypeUtil.getAtlasObjectId(db));
        entity.setAttribute("columns", AtlasTypeUtil.toObjectIds(columns));

        return createInstance(entity, traitNames);
    }

    AtlasEntity createProcess(String name, String description, String user, List<AtlasEntity> inputs, List<AtlasEntity> outputs,
                              String queryText, String queryPlan, String queryId, String queryGraph, String... traitNames) throws Exception {
        AtlasEntity entity = new AtlasEntity(LOAD_PROCESS_TYPE);

        entity.setClassifications(toAtlasClassifications(traitNames));
        entity.setAttribute(AtlasClient.NAME, name);
        entity.setAttribute(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME, name);
        entity.setAttribute("description", description);
        entity.setAttribute("inputs", inputs);
        entity.setAttribute("outputs", outputs);
        entity.setAttribute("user", user);
        entity.setAttribute("startTime", System.currentTimeMillis());
        entity.setAttribute("endTime", System.currentTimeMillis() + 10000);
        entity.setAttribute("queryText", queryText);
        entity.setAttribute("queryPlan", queryPlan);
        entity.setAttribute("queryId", queryId);
        entity.setAttribute("queryGraph", queryGraph);

        return createInstance(entity, traitNames);
    }


    private void lineage() throws AtlasServiceException {
        System.out.println("\nSample Lineage Info: ");

        AtlasLineageInfo lineageInfo = atlasClientV2.getLineageInfo(getTableId(LOG_DAILY_MV), AtlasLineageInfo.LineageDirection.BOTH, 0);
        Set<AtlasLineageInfo.LineageRelation> relations = lineageInfo.getRelations();
        Map<String, AtlasEntityHeader> guidEntityMap = lineageInfo.getGuidEntityMap();

        for (AtlasLineageInfo.LineageRelation relation : relations) {
            AtlasEntityHeader fromEntity = guidEntityMap.get(relation.getFromEntityId());
            AtlasEntityHeader toEntity = guidEntityMap.get(relation.getToEntityId());

            System.out.println(fromEntity.getDisplayText() + "(" + fromEntity.getTypeName() + ") -> " +
                    toEntity.getDisplayText() + "(" + toEntity.getTypeName() + ")");
        }
    }

    private String getTableId(String tableName) throws AtlasServiceException {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME, tableName);

        AtlasEntity tableEntity = atlasClientV2.getEntityByAttribute(TABLE_TYPE, attributes).getEntity();
        return tableEntity.getGuid();
    }
}