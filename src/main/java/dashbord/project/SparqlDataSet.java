package dashbord.project;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.dashbuilder.dataset.DataSetFactory.newDataSetBuilder;

public class SparqlDataSet {
    private ResultSet resultSet;
    private DataSet dataSet;

    public SparqlDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public SparqlDataSet(ResultSet set) {
        this.resultSet = set;
    }

    public DataSet getDataSet(){return dataSet;}

    public ResultSet getResultSet() {return resultSet;}

    public DataSet createDataSet(){

        var dataSetBuilder = newDataSetBuilder();
        fillDataSet(this.resultSet, dataSetBuilder, true);

        this.dataSet = dataSetBuilder.buildDataSet();
        return this.dataSet;
    }


    private void fillDataSet(ResultSet set, DataSetBuilder dataSetBuilder, Boolean getColumns) {

        while (set.hasNext()){

            QuerySolution qs = set.next();
            List<String> row_strings = new ArrayList<>();

            for (Iterator<String> it = qs.varNames(); it.hasNext(); ) {
                String varName = it.next();

                if(getColumns) {
                    ColumnType type = getColumnType(qs, varName);
                    dataSetBuilder.column(varName, type);
                }

                //Literal or other to get value
                if(qs.get(varName).isLiteral()){
                    row_strings.add(qs.getLiteral(varName).getValue().toString());
                }else {
                    row_strings.add(qs.get(varName).toString());
                }
            }
            //One pass for columns
            getColumns = Boolean.FALSE;

            //To Array necessary for row method
            dataSetBuilder.row(row_strings.toArray());
        }
    }


    private ColumnType getColumnType(QuerySolution qs, String varName) {
        ColumnType type = getResourceType(qs, varName);
        if (type != null){
            return type;
        }

        type = getLiteralType(qs, varName);
        if (type != null){
            return type;
        }

        return ColumnType.LABEL;
    }

    private ColumnType getResourceType(QuerySolution qs, String varName){
        if (!qs.get(varName).isResource()) return null;
        try {
            Resource resource = qs.getResource(varName);
            if (resource != null) {
                return ColumnType.LABEL;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getClass().toString());
        }
        return null;
    }

    private ColumnType getLiteralType(QuerySolution qs, String varName){
        if (!qs.get(varName).isLiteral()) return null;
        try {
            Literal literal = qs.getLiteral(varName);
            if (literal != null){
                Class<?> literalClass = literal.getDatatype().getJavaClass();
                if (Number.class.isAssignableFrom(literalClass)) {
                    return ColumnType.NUMBER;
                }
                if (LocalDateTime.class.isAssignableFrom(literalClass) ||
                        LocalDate.class.isAssignableFrom(literalClass) ||
                        LocalTime.class.isAssignableFrom(literalClass))
                {
                    return ColumnType.DATE;
                }
                return ColumnType.LABEL;
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getClass().toString());
        }
        return null;
    }
}

