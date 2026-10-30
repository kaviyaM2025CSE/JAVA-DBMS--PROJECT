import java.sql.*;
import java.util.ArrayList;
import java.util.List;

interface ITaskRepository {
    void addTask(String title, String priority) throws SQLException;
    List<Task> getAllTasks() throws SQLException;
    void updateTaskStatus(int id, String status) throws SQLException; 
    void deleteTask(int id) throws SQLException;                     
}

public class TaskService implements ITaskRepository {
    
    @Override
    public void addTask(String title, String priority) throws SQLException {
        String sql = "INSERT INTO tasks (title, priority, assigned_user_id) VALUES (?, ?, 1)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, priority);
            pstmt.executeUpdate();
            System.out.println("🎉 DBMS Create: Task Inserted Successfully!");
        }
    }

    @Override
    public List<Task> getAllTasks() throws SQLException {
        List<Task> taskList = new ArrayList<>();
        String sql = "SELECT id, title, status, priority FROM tasks";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                taskList.add(new Task(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        }
        return taskList;
    }

    @Override
    public void updateTaskStatus(int id, String status) throws SQLException {
        
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("✅ DBMS Update: Task ID " + id + " Status marked as " + status);
        }
    }

    @Override
    public void deleteTask(int id) throws SQLException {
       
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("🗑️ DBMS Delete: Task ID " + id + " removed from repositories.");
        }
    }
}
