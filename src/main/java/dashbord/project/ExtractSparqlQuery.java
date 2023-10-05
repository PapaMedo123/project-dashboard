package dashbord.project;

import org.apache.jena.query.*;


public class ExtractSparqlQuery {

    private String sparqlService;
    private String queryString;

    public ExtractSparqlQuery() {}

    public ExtractSparqlQuery(String sparqlService, String queryString) {
        this.sparqlService = sparqlService;
        this.queryString = queryString;
    }

    public void setSparqlService(String sparqlService) {
        this.sparqlService = sparqlService;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getSparqlService() {
        return sparqlService;
    }

    public String getQueryString() {
        return queryString;
    }

    public ResultSet getResultSet() throws Exception {
        ResultSetRewindable set;

        //Throws
        set = getResultViaSerice();

        if (!set.hasNext()) {
            throw new NotGoodQuery("Something is wrong with the query string");
        }

        return set.materialise();
    }

    public ResultSetRewindable getResultViaSerice (String sparqlService, String queryString){
        try (QueryExecution qexec = QueryExecution
                .service(sparqlService)
                .query(queryString).build()) {
            ResultSet results = qexec.execSelect();
            return ResultSetFactory.copyResults(results); // Passes the result set out of the try-resources
        }
    }

    public ResultSetRewindable getResultViaSerice (){
        try (QueryExecution qexec = QueryExecution
                .service(this.sparqlService)
                        // Jena bug
              //  + "?force=true")
                .query(prefixes + this.queryString).build()) {
            ResultSet results = qexec.execSelect();
            return ResultSetFactory.copyResults(results); // Passes the result set out of the try-resources
        }
    }

    public static ResultSet test1() {
        String sparqlService = "http://dbpedia.org/sparql";

        String queryString = "PREFIX dbp: <http://dbpedia.org/property/> " +
                "PREFIX dbo: <http://dbpedia.org/ontology/> " +
                "PREFIX dbr: <http://dbpedia.org/resource/> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns> " +
                "PREFIX gold: <http://linguistlist.org> " +
//                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "SELECT DISTINCT ?Name ?Area " +
                "WHERE { " +
                "?Country a dbo:Country ; " +
                "           dbo:area ?Area; " +
                "           dbp:conventionalLongName ?Name ; " +
                "           dbo:countryCode [] . " +
                "FILTER NOT EXISTS { ?Country dbo:dissolutionYear [] } . " +
                "FILTER NOT EXISTS { ?Country dbp:dateEnd [] } . " +
                "FILTER ( LANG(?Name) = \"en\" && ?Name != \"\"@en ) . " +
//                "FILTER NOT EXISTS { ?Country dbp:orgType [] } . " +
                "}";

        ExtractSparqlQuery extractSparqlQuery = new ExtractSparqlQuery(sparqlService, queryString);

        ResultSetRewindable set;
        try {
            set = extractSparqlQuery.getResultViaSerice(sparqlService, queryString);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong");
            return null;
        }

        return set.materialise();
    }

    public static String prefixes =
            "PREFIX dbp: <http://dbpedia.org/property/> " +
                    "PREFIX dbo: <http://dbpedia.org/ontology/> " +
                    "PREFIX dbr: <http://dbpedia.org/resource/> " +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns> " +
                    "PREFIX gold: <http://linguistlist.org> " +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema> "+
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                    "PREFIX bd: <http://www.bigdata.com/rdf#> " +
                    "PREFIX cc: <http://creativecommons.org/ns#> " +
                    "PREFIX dct: <http://purl.org/dc/terms/> " +
                    "PREFIX geo: <http://www.opengis.net/ont/geosparql#> " +
                    "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#> ";
}

class NotGoodQuery extends Exception{

    public NotGoodQuery(String message) {
        super(message);
    }
}
