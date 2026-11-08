/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 96.81146025878003, "KoPercent": 3.188539741219963};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9634935304990758, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.0, 500, 1500, "Voluntario: GET /api/usuarios (debe ser 403)"], "isController": false}, {"data": [0.0, 500, 1500, "Sin token: POST /api/cajas"], "isController": false}, {"data": [0.0, 500, 1500, "POST crear caja"], "isController": false}, {"data": [0.0, 500, 1500, "DELETE eliminar caja"], "isController": false}, {"data": [0.9970588235294118, 500, 1500, "GET detalle de caja"], "isController": false}, {"data": [0.9936274509803922, 500, 1500, "GET inventario (dashboard)"], "isController": false}, {"data": [0.5, 500, 1500, "Crear voluntario de prueba"], "isController": false}, {"data": [0.996078431372549, 500, 1500, "GET búsqueda/filtro"], "isController": false}, {"data": [0.0, 500, 1500, "Token falso: GET /api/cajas"], "isController": false}, {"data": [0.0, 500, 1500, "Login con clave incorrecta"], "isController": false}, {"data": [0.9980392156862745, 500, 1500, "GET escaneo QR"], "isController": false}, {"data": [0.0, 500, 1500, "PUT actualizar caja"], "isController": false}, {"data": [0.9607843137254902, 500, 1500, "Login voluntario"], "isController": false}, {"data": [0.0, 500, 1500, "Voluntario: POST /api/cajas (debe ser 403)"], "isController": false}, {"data": [1.0, 500, 1500, "Crear caja semilla 2"], "isController": false}, {"data": [0.0, 500, 1500, "Eliminar voluntario de prueba"], "isController": false}, {"data": [1.0, 500, 1500, "Crear caja semilla 1"], "isController": false}, {"data": [0.0, 500, 1500, "PATCH despachar caja"], "isController": false}, {"data": [0.0, 500, 1500, "Sin token: GET /api/cajas"], "isController": false}, {"data": [1.0, 500, 1500, "Crear caja semilla 5"], "isController": false}, {"data": [1.0, 500, 1500, "Crear caja semilla 4"], "isController": false}, {"data": [0.08333333333333333, 500, 1500, "Login admin"], "isController": false}, {"data": [1.0, 500, 1500, "Crear caja semilla 3"], "isController": false}, {"data": [1.0, 500, 1500, "Voluntario: GET /api/cajas (permitido)"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 4328, 138, 3.188539741219963, 224.86922365988895, 1, 2093, 186.0, 351.0, 362.0, 481.4200000000001, 9.793007322128394, 11.666188404093921, 3.8660206135676596], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["Voluntario: GET /api/usuarios (debe ser 403)", 1, 1, 100.0, 102.0, 102, 102, 102.0, 102.0, 102.0, 102.0, 9.803921568627452, 3.6381740196078436, 3.839231004901961], "isController": false}, {"data": ["Sin token: POST /api/cajas", 1, 1, 100.0, 2.0, 2, 2, 2.0, 2.0, 2.0, 2.0, 500.0, 185.546875, 118.1640625], "isController": false}, {"data": ["POST crear caja", 30, 30, 100.0, 4.033333333333334, 2, 12, 3.0, 9.900000000000002, 10.899999999999999, 12.0, 3.3123550844650547, 1.2291942696257039, 0.9836788878767804], "isController": false}, {"data": ["DELETE eliminar caja", 30, 30, 100.0, 3.3, 1, 9, 3.0, 5.0, 6.799999999999997, 9.0, 3.288391976323578, 1.2203017099638278, 0.773928189740217], "isController": false}, {"data": ["GET detalle de caja", 1020, 0, 0.0, 224.5166666666665, 139, 2093, 183.5, 347.0, 360.0, 431.5799999999999, 2.479079726717917, 1.4017452751656974, 1.0240729730485147], "isController": false}, {"data": ["GET inventario (dashboard)", 1020, 0, 0.0, 232.55392156862735, 140, 1703, 188.0, 351.9, 361.94999999999993, 556.5299999999997, 2.491335282436819, 5.617669108541617, 0.968311955478373], "isController": false}, {"data": ["Crear voluntario de prueba", 1, 0, 0.0, 533.0, 533, 533, 533.0, 533.0, 533.0, 533.0, 1.876172607879925, 0.9344219043151969, 0.8959457082551594], "isController": false}, {"data": ["GET búsqueda/filtro", 1020, 0, 0.0, 232.4382352941178, 139, 914, 189.0, 355.9, 366.0, 493.89999999999964, 2.4887821803195886, 3.854695837877801, 1.003776406710928], "isController": false}, {"data": ["Token falso: GET /api/cajas", 1, 1, 100.0, 8.0, 8, 8, 8.0, 8.0, 8.0, 8.0, 125.0, 46.38671875, 26.9775390625], "isController": false}, {"data": ["Login con clave incorrecta", 1, 1, 100.0, 177.0, 177, 177, 177.0, 177.0, 177.0, 177.0, 5.649717514124294, 2.9848605225988702, 1.4896716101694916], "isController": false}, {"data": ["GET escaneo QR", 1020, 0, 0.0, 224.2333333333332, 138, 1394, 181.5, 348.0, 360.0, 432.6399999999994, 2.4724096055537106, 1.397973790640233, 0.9947585522345008], "isController": false}, {"data": ["PUT actualizar caja", 30, 30, 100.0, 3.1333333333333333, 2, 5, 3.0, 4.900000000000002, 5.0, 5.0, 3.3894475200542313, 1.2578027906451248, 1.0062422325160998], "isController": false}, {"data": ["Login voluntario", 102, 0, 0.0, 348.2450980392157, 225, 1773, 304.5, 414.80000000000007, 957.9999999999983, 1756.8299999999995, 0.3314647818669266, 0.2052233122105776, 0.09095859736777967], "isController": false}, {"data": ["Voluntario: POST /api/cajas (debe ser 403)", 1, 1, 100.0, 109.0, 109, 109, 109.0, 109.0, 109.0, 109.0, 9.174311926605505, 3.4045298165137616, 4.067517201834862], "isController": false}, {"data": ["Crear caja semilla 2", 1, 0, 0.0, 331.0, 331, 331, 331.0, 331.0, 331.0, 331.0, 3.0211480362537766, 1.802657666163142, 1.3483053247734138], "isController": false}, {"data": ["Eliminar voluntario de prueba", 1, 1, 100.0, 7.0, 7, 7, 7.0, 7.0, 7.0, 7.0, 142.85714285714286, 53.013392857142854, 36.83035714285714], "isController": false}, {"data": ["Crear caja semilla 1", 1, 0, 0.0, 336.0, 336, 336, 336.0, 336.0, 336.0, 336.0, 2.976190476190476, 1.7758324032738093, 1.328241257440476], "isController": false}, {"data": ["PATCH despachar caja", 30, 30, 100.0, 2.933333333333333, 2, 5, 3.0, 4.0, 5.0, 5.0, 3.374198627825891, 1.2521440220447644, 0.8929763946687662], "isController": false}, {"data": ["Sin token: GET /api/cajas", 1, 1, 100.0, 14.0, 14, 14, 14.0, 14.0, 14.0, 14.0, 71.42857142857143, 26.506696428571427, 12.974330357142858], "isController": false}, {"data": ["Crear caja semilla 5", 1, 0, 0.0, 309.0, 309, 309, 309.0, 309.0, 309.0, 309.0, 3.236245954692557, 1.931002224919094, 1.4443011731391586], "isController": false}, {"data": ["Crear caja semilla 4", 1, 0, 0.0, 320.0, 320, 320, 320.0, 320.0, 320.0, 320.0, 3.125, 1.8646240234375, 1.3946533203125], "isController": false}, {"data": ["Login admin", 12, 11, 91.66666666666667, 207.5, 168, 473, 177.5, 408.80000000000024, 473.0, 473.0, 0.027153007195546905, 0.014509004333167399, 0.0070512081673982895], "isController": false}, {"data": ["Crear caja semilla 3", 1, 0, 0.0, 337.0, 337, 337, 337.0, 337.0, 337.0, 337.0, 2.967359050445104, 1.7705628709198813, 1.3242998887240356], "isController": false}, {"data": ["Voluntario: GET /api/cajas (permitido)", 1, 0, 0.0, 219.0, 219, 219, 219.0, 219.0, 219.0, 219.0, 4.5662100456621, 10.29626855022831, 1.774757420091324], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["401", 12, 8.695652173913043, 0.27726432532347506], "isController": false}, {"data": ["403", 126, 91.30434782608695, 2.911275415896488], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 4328, 138, "403", 126, "401", 12, "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["Voluntario: GET /api/usuarios (debe ser 403)", 1, 1, "403", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Sin token: POST /api/cajas", 1, 1, "403", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["POST crear caja", 30, 30, "403", 30, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["DELETE eliminar caja", 30, 30, "403", 30, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["Token falso: GET /api/cajas", 1, 1, "403", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Login con clave incorrecta", 1, 1, "401", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["PUT actualizar caja", 30, 30, "403", 30, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["Voluntario: POST /api/cajas (debe ser 403)", 1, 1, "403", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["Eliminar voluntario de prueba", 1, 1, "403", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["PATCH despachar caja", 30, 30, "403", 30, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["Sin token: GET /api/cajas", 1, 1, "403", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["Login admin", 12, 11, "401", 11, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
