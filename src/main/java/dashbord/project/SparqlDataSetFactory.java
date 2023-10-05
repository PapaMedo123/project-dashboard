package dashbord.project;

import org.apache.jena.query.ResultSet;
import org.dashbuilder.dataset.DataSet;

public class SparqlDataSetFactory {

    private ExtractSparqlQuery extractSparqlQuery;
    private SparqlDataSet sparqlDataSet;

    public SparqlDataSetFactory() {}

    public DataSet setDataSet(String sparqlService, String queryString) throws Exception {
        extractSparqlQuery = new ExtractSparqlQuery(sparqlService,queryString);
        ResultSet resultSet = null;
        try {
            resultSet = extractSparqlQuery.getResultSet();
        } catch (Exception e){
            System.out.println(e.getMessage());
            throw e;
        }
        sparqlDataSet = new SparqlDataSet(resultSet);
        return sparqlDataSet.createDataSet();
    }

    public DataSet setDataSet(ExtractSparqlQuery extractSparqlQuery) throws Exception {
        ResultSet resultSet = null;
        try {
            resultSet = extractSparqlQuery.getResultSet();
        } catch (Exception e){
            System.out.println(e.getMessage());
            throw e;
        }
        sparqlDataSet = new SparqlDataSet(resultSet);
        return sparqlDataSet.createDataSet();
    }

    public DataSet getDataSet(){return sparqlDataSet.getDataSet();}

    public ResultSet getResultSet(){return sparqlDataSet.getResultSet();}
}