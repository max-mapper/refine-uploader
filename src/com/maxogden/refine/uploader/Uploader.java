package com.maxogden.refine.uploader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONWriter;
import com.google.refine.commands.Command;
import com.google.refine.ProjectManager;
import com.google.refine.model.Project;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Column;
import com.google.refine.model.Row;
import com.google.refine.browsing.Engine;
import com.google.refine.browsing.FilteredRows;
import com.google.refine.browsing.RowVisitor;
import com.google.refine.util.ParsingUtilities;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.HttpStatus;

public class Uploader extends Command {
    protected RowVisitor createRowVisitor(Project project, List<Object> values) throws Exception {
        return new RowVisitor() {
            List<Object> values;
            
            public RowVisitor init(List<Object> values) {
                this.values = values;
                return this;
            }
            
            @Override
            public void start(Project project) {
            	// nothing to do
            }
            
            @Override
            public void end(Project project) {
            	// nothing to do
            }
            
            public boolean visit(Project project, int rowIndex, Row row) {
              int size = project.columnModel.columns.size();

              String[] vals = new String[size];

              int i = 0;
              for (Column col : project.columnModel.columns) {
                  int cellIndex = col.getCellIndex();
                  Object value = row.getCellValue(cellIndex);
                  if (value != null) {
                      vals[i] = value instanceof String ? (String) value : value.toString();
                  }
                  i++;
              }
              this.values.add(vals);

              return false;
            }
        }.init(values);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    };
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {            
        try {
            ProjectManager.singleton.setBusy(true);
            Project project = getProject(request);
            ColumnModel columnModel = project.columnModel;
            
            List<Object> values = new ArrayList<Object>();
            List<String> columns = new ArrayList<String>();

            Engine engine = new Engine(project);
            JSONObject engineConfig = null;

            try {
                engineConfig = ParsingUtilities.evaluateJsonStringToObject(request.getParameter("engine"));
            } catch (JSONException e) {
                // ignore
            }

            engine.initializeFromJSON(engineConfig);

            FilteredRows filteredRows = engine.getAllFilteredRows();
            filteredRows.accept(project, createRowVisitor(project, values));
            
            JSONArray stuff = new JSONArray();

            for (Column col : project.columnModel.columns) {
                columns.add(col.getName());
            }

            for (int num = 0; num < values.size(); num++) {
              int y = 0;
              String[] a = (String[]) values.get(num);
              JSONObject rowdata = new JSONObject();
              for (int x = 0; x < a.length; x++) {
                if(a[x] instanceof String) {
                  if(a[x].contains("{\\\"")) {
                    JSONObject j;
                    j = new JSONObject(a[x].replace("\\\"", "\""));
                    rowdata.put(columns.get(x), j);
                  } else {
                    rowdata.put(columns.get(x), a[x]);
                  }
                }
                y++;
              }
              stuff.put(rowdata);
            }

            JSONObject bulkwrapper = new JSONObject();
            bulkwrapper.put("docs", stuff);
            String jsondata = bulkwrapper.toString();
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpost = new HttpPost(request.getParameter("url"));
            StringEntity se = new StringEntity(jsondata);
            httpost.setEntity(se);
            httpost.setHeader("Accept", "application/json");
            httpost.setHeader("Content-type", "application/json");
            ResponseHandler responseHandler = new BasicResponseHandler();
            httpclient.execute(httpost, responseHandler);
            
            JSONWriter writer = new JSONWriter(response.getWriter());
            writer.object();
            writer.key("ok");
            writer.value("true");
            writer.endObject();
            
        } catch (Exception e) {
            respondException(response, e);
        } finally {
            ProjectManager.singleton.setBusy(false);
        }
    };
}

