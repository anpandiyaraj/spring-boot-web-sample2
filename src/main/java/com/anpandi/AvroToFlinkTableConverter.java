package com.anpandi;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AvroToFlinkTableConverter {

    public static String avroToFlinkTable(String avroSchemaString, String flinkTableName) throws IOException {
        // Parse the Avro schema string
        Map<String, Object> avroSchema = new ObjectMapper().readValue(avroSchemaString, Map.class);

        // Extract table schema and field types
        StringBuilder tableSchema = new StringBuilder();
        for (Map<String, Object> field : (List<Map<String, Object>>) avroSchema.get("fields")) {
            String fieldName = (String) field.get("name");
            String fieldType = (String) field.get("type");
            switch (fieldType) {
                case "string":
                    fieldType = "VARCHAR";
                    break;
                case "int":
                    fieldType = "INT";
                    break;
                case "long":
                    fieldType = "BIGINT";
                    break;
                case "float":
                    fieldType = "FLOAT";
                    break;
                case "double":
                    fieldType = "DOUBLE";
                    break;
                case "boolean":
                    fieldType = "BOOLEAN";
                    break;
                case "bytes":
                    fieldType = "BINARY";
                    break;
                default:
                    fieldType = "STRING"; // Handle unsupported types as strings
            }

            // Handle nested fields and arrays
            if (fieldType.startsWith("array")) {
                fieldType = "ARRAY<" + fieldType.substring(6, fieldType.length() - 1) + ">";
            } else if (fieldType.startsWith("record")) {
                fieldType = "ROW<" + avroToFlinkTableSchema((List<Map<String, Object>>) field.get("fields")) + ">";
            }

            tableSchema.append("`").append(fieldName).append("` ").append(fieldType).append(", ");
        }

        // Remove the trailing comma and space
        if (tableSchema.length() > 0) {
            tableSchema.setLength(tableSchema.length() - 2);
        }

        // Create the Flink table script
        String flinkTableScript = String.format("CREATE TABLE %s (\n %s\n) WITH (\n 'connector.type' = 'kafka',\n 'connector.version' = 'universal',\n 'connector.topic' = 'your_topic',\n 'connector.properties.bootstrap.servers' = 'your_kafka_bootstrap_servers',\n 'format.type' = 'avro'\n)", flinkTableName, tableSchema);

        return flinkTableScript;
    }

    private static String avroToFlinkTableSchema(List<Map<String, Object>> fields) {
        StringBuilder schemaFields = new StringBuilder();
        for (Map<String, Object> field : fields) {
            schemaFields.append("`").append(field.get("name")).append("` ").append(avroToFlinkTableType((String) field.get("type"))).append(", ");
        }
        // Remove the trailing comma and space
        if (schemaFields.length() > 0) {
            schemaFields.setLength(schemaFields.length() - 2);
        }
        return schemaFields.toString();
    }

    private static String avroToFlinkTableType(String avroType) {
        switch (avroType) {
            case "string":
                return "VARCHAR";
            case "int":
                return "INT";
            case "long":
                return "BIGINT";
            case "float":
                return "FLOAT";
            case "double":
                return "DOUBLE";
            case "boolean":
                return "BOOLEAN";
            case "bytes":
                return "BINARY";
            default:
                return "STRING"; // Handle unsupported types as strings
        }
    }
//Test
    public static void main(String[] args) throws IOException {
        String avroSchemaString = "{\"name\":\"Person\",\"type\":\"record\",\"fields\":[{\"name\":\"firstName\",\"type\":\"string\"},{\"name\":\"lastName\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"}]}";
        String flinkTableName = "person_table";
        String flinkTableScript = avroToFlinkTable(avroSchemaString, flinkTableName);
        System.out.println(flinkTableScript);
    }
}
