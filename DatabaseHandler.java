package fitz;


import java.sql.*;


public class DatabaseHandler {
    private Connection con;
    public  DatabaseHandler(){

        try{

            Class.forName("org.mariadb.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mariadb://localhost/fitz","root","developers");
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public int executeGetId(String sql, Object[] param){
        int result = 0;
        try{
            PreparedStatement statement = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            for(int i = 0; i < param.length; i++){
                int d = i + 1;
                statement.setObject(d, param[i]);
            }
            statement.executeUpdate();
            ResultSet id = statement.getGeneratedKeys();
            if(id.next()){
                return id.getInt(1);
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return  result;
    }
    public  void  execute(String sql, Object[] param){
        try{
            PreparedStatement statement = con.prepareStatement(sql);
            for(int i = 0; i < param.length; i++){
                int d = i + 1;
                statement.setObject(d, param[i]);
            }
            statement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public ResultSet getAll(String sql, Object[] param){
        ResultSet result = null;
        try{
            PreparedStatement statement = con.prepareStatement(sql);
            for(int i = 0; i < param.length; i++){
                int d = i + 1;
                statement.setObject(d, param[i]);
            }
            result = statement.executeQuery();
            return  result;

        }catch (SQLException e){
            e.printStackTrace();
        }
        return result;
    }
    public  ResultSet getAll(String sql){
        ResultSet result = null;
        try{
             Statement statement = con.createStatement();
            result = statement.executeQuery(sql);

            return  result;

        }catch (SQLException e){
            e.printStackTrace();
        }
        return result;
    }
}
