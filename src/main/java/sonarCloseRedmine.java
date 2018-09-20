
import com.taskadapter.redmineapi.*;
import com.taskadapter.redmineapi.bean.Issue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class sonarCloseRedmine {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://url/sonar";
    private static final String APIKEY = "";
    private static final String RedmineURL = "";
    private static final String USER = "";
    private static final String PASS = "";

    public static void main(String[] args) {
        RedmineManager mgr = RedmineManagerFactory.createWithApiKey(RedmineURL, APIKEY);
        IssueManager issueManager = mgr.getIssueManager();
        Connection conn = null;
        Statement stmt = null;
        String first = null;
        List<String> resultList = new ArrayList<String>();
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql;
            sql = "select change_data as redmineIssue from issue_changes where change_type='comment' and change_data like '%/issues/%' and issue_key in (select kee from issues where status != 'OPEN');";
            ResultSet rs = stmt.executeQuery(sql);
            int i = 0;
            while (rs.next()) {
                first = rs.getString("redmineIssue");
                System.out.println("Data:" + first);
                Pattern p = Pattern.compile("[0-9]+$");
                Matcher m = p.matcher(first);
                if (m.find()) {
                    resultList.add(m.group());
                    Issue issue = issueManager.getIssueById(Integer.valueOf(resultList.get(i)));
                    if (issue.getStatusId()!=5) {
                        String message="sonarqube closed.";
                        issue.setStatusId(5);
                        issue.setNotes(message);
                        mgr.getIssueManager().update(issue);
                    }
                    i++;
                }
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (IllegalStateException IIIE) {
            IIIE.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (NotFoundException nf) {
            nf.printStackTrace();
        } catch (
                Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
