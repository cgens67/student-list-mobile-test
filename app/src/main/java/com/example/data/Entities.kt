package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cn: String,
    val oldClass: String,
    val newClass: String,
    val sch: String
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val className: String,
    val guru: String,
    val penolong: String
)

@Entity(tableName = "class_notices")
data class ClassNoticeEntity(
    @PrimaryKey val className: String,
    val text: String,
    val icon: String,
    val color: String,
    val style: String
)

@Entity(tableName = "duty_roster")
data class DutyRosterEntity(
    @PrimaryKey val day: String,
    val wipeList: String, // comma separated IDs
    val sweepList: String,
    val arrangeList: String,
    val trashList: String,
    val unitKebersihan: String
)

@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudent(id: String)

    @Update
    suspend fun updateStudent(student: StudentEntity)
    
    // Teachers
    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)

    @Query("DELETE FROM teachers")
    suspend fun deleteAllTeachers()

    // Class Notices
    @Query("SELECT * FROM class_notices")
    fun getAllNotices(): Flow<List<ClassNoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<ClassNoticeEntity>)

    @Query("DELETE FROM class_notices")
    suspend fun deleteAllNotices()

    // Duty Rosters
    @Query("SELECT * FROM duty_roster")
    fun getAllDutyRosters(): Flow<List<DutyRosterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDutyRosters(rosters: List<DutyRosterEntity>)

    @Query("DELETE FROM duty_roster")
    suspend fun deleteAllDutyRosters()
}

@Database(entities = [
    StudentEntity::class,
    TeacherEntity::class,
    ClassNoticeEntity::class,
    DutyRosterEntity::class
], version = 1, exportSchema = false)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "student_allocation_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
