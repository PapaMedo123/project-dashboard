
package dashbord.project;

import dashbord.project.web.ChartType;
import org.apache.jena.query.*;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.dsl.model.Row;
import org.dashbuilder.dsl.serialization.DashboardExporter;
import org.dashbuilder.dsl.serialization.DashboardExporter.ExportType;
import org.uberfire.ext.widgets.common.client.common.PrettyFormLayout;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.dashbuilder.dataset.DataSetFactory.newEmptyDataSet;
import static org.dashbuilder.displayer.DisplayerSettingsFactory.newBarChartSettings;
import static org.dashbuilder.dsl.factory.component.ComponentFactory.displayer;
import static org.dashbuilder.dsl.factory.dashboard.DashboardFactory.dashboard;
import static org.dashbuilder.dsl.factory.page.PageFactory.*;

public class DashboardMaker {

    public boolean createDashboard(String sparqlService, String queryString, ChartType chartType, String zipFileName, String columnID) throws Exception {
        if (sparqlService.isBlank() || sparqlService.isEmpty()) {
            sparqlService = DashboardMaker.sparqlService;
        }

        displayChart(sparqlService, queryString, columnID, zipFileName, chartType);

        return true;
    }

    public void displayChart(String sparqlService, String queryString, String ColumnID, String zipFileName, ChartType chartType) throws Exception {
        SparqlDataSetFactory s = new SparqlDataSetFactory();

        DataSet dataSet = newEmptyDataSet();
        try {
            dataSet = s.setDataSet(sparqlService, queryString);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        var columns = dataSet.getColumns();

        List<Row> rowList = new ArrayList<>();
        rowList.add(row("<h3 class = 'text-center h5 text-primary -align-center -align-justify'> Countries Population</h3>"));

        switch (chartType) {
            case LINE -> rowList.add(row(buildGraphChart(dataSet, columns, ColumnID)));
            case AREA -> rowList.add(row(buildAreaChart(dataSet, columns, ColumnID)));
            case PIE -> rowList.add(row(buildPieChart(dataSet, columns, ColumnID)));
            case BAR -> rowList.add(row(buildPopBarChart(dataSet, columns)));
            case MAP -> rowList.add(row(buildMapChart(dataSet, columns)));
            case ALL -> rowList.addAll(List.of(
                    row(buildGraphChart(dataSet, columns, ColumnID)),
                    row(buildAreaChart(dataSet, columns, ColumnID)),
                    row(buildPieChart(dataSet, columns, ColumnID)),
                    row(buildPopBarChart(dataSet, columns)),
                    row(buildMapChart(dataSet, columns))
            ));
            default -> throw new NotGoodQuery("Char Type Error");
        }

        var page = page("SPARQL Dashbuilder",
                rowList.toArray(new Row[0])
        );

        DashboardExporter.get().export(dashboard(asList(page)),
                "./dashbuilder-runtime/" +
                        "dashboards/" +
                        zipFileName + ".zip",
                ExportType.ZIP);
    }

    public DisplayerSettings buildPopBarChart(DataSet dataSet, List<DataColumn> columns) {
        var popBarChartBuilder = newBarChartSettings().subType_Column()
                .width(800)
                .height(600)
                .dataset(dataSet);

        for (DataColumn column : columns) {
            popBarChartBuilder.column(column.getId());
        }

        return popBarChartBuilder.buildSettings();
    }

    public DisplayerSettings buildGraphChart(DataSet dataSet, List<DataColumn> columns, String ColumnID) {

        var graphChartBuilder = DisplayerSettingsFactory.newLineChartSettings()
                .width(800)
                .height(600)
                .dataset(dataSet);

        if (ColumnID != null && ColumnID.length() > 0) {
            DataColumn groupColumn = dataSet.getColumnById(ColumnID);
            if (groupColumn == null) {
                graphChartBuilder.group(columns.get(0).getId());
            } else {
                graphChartBuilder.group(ColumnID);
            }
        } else {
            graphChartBuilder.group(columns.get(0).getId());
        }

        for (DataColumn column : columns) {
            graphChartBuilder.column(column.getId());
        }

        return graphChartBuilder.buildSettings();
    }

    public DisplayerSettings buildAreaChart(DataSet dataSet, List<DataColumn> columns, String ColumnID) {
        var areaChartBuilder = DisplayerSettingsFactory.newAreaChartSettings()
                .width(800)
                .height(600)
                .dataset(dataSet);

        if (ColumnID != null && ColumnID.length() > 0) {
            DataColumn groupColumn = dataSet.getColumnById(ColumnID);
            if (groupColumn == null) {
                areaChartBuilder.group(columns.get(0).getId());
            } else {
                areaChartBuilder.group(ColumnID);
            }
        } else {
            areaChartBuilder.group(columns.get(0).getId());
        }

        for (DataColumn column : columns) {
            areaChartBuilder.column(column.getId());
        }

        return areaChartBuilder.buildSettings();
    }

    public DisplayerSettings buildPieChart(DataSet dataSet, List<DataColumn> columns, String ColumnID) {

        DataSet datSetClo = dataSet.cloneInstance();


        // First Column is always the names and labels
        boolean firstColumn = true;
        boolean secondColumn = false;

        // If column id is not provided
        if (ColumnID == null || ColumnID.isEmpty() || ColumnID.isBlank()) {
            secondColumn = true;
        }

            for (DataColumn column : columns) {
            if (firstColumn){
                firstColumn = false;
                continue;
            }
            if (secondColumn){
                secondColumn = false;
                continue;
            } else if (column.getId().equals(ColumnID)){
                continue;
            }
            datSetClo.removeColumn(column.getId());
        }

        var pieChartBuilder = DisplayerSettingsFactory.newPieChartSettings()
                .width(800)
                .height(600)
                .dataset(datSetClo);

        for (DataColumn column : columns) {
            pieChartBuilder.column(column.getId());
        }

        return pieChartBuilder.buildSettings();
    }

    public DisplayerSettings buildMapChart(DataSet dataSet, List<DataColumn> columns) {
        var mapChartBuilder = DisplayerSettingsFactory.newMapChartSettings()
                .width(1200)
                .height(900)
                .dataset(dataSet);

        for (DataColumn column : columns) {
            mapChartBuilder.column(column.getId());
        }

        return mapChartBuilder.buildSettings();
    }

    public static final String sparqlService = "http://dbpedia.org/sparql";

    public static final String queryString = "PREFIX dbp: <http://dbpedia.org/property/> " +
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

    public static final String prefixes =
            "PREFIX dbp: <http://dbpedia.org/property/> " +
                    "PREFIX dbo: <http://dbpedia.org/ontology/> " +
                    "PREFIX dbr: <http://dbpedia.org/resource/> " +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns> " +
                    "PREFIX gold: <http://linguistlist.org> " +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema> " +
                    "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";
}