package com.todoroo.astrid.adapter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.natpryce.makeiteasy.MakeItEasy.with
import com.natpryce.makeiteasy.PropertyValue
import com.todoroo.astrid.core.BuiltInFilterExposer
import com.todoroo.astrid.dao.TaskDaoBlocking
import com.todoroo.astrid.data.Task
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.tasks.LocalBroadcastManager
import org.tasks.data.CaldavDaoBlocking
import org.tasks.data.GoogleTaskDaoBlocking
import org.tasks.data.TaskContainer
import org.tasks.data.TaskListQuery.getQuery
import org.tasks.injection.InjectingTestCase
import org.tasks.injection.ProductionModule
import org.tasks.makers.TaskMaker.PARENT
import org.tasks.makers.TaskMaker.newTask
import org.tasks.preferences.Preferences
import javax.inject.Inject

@UninstallModules(ProductionModule::class)
@HiltAndroidTest
class OfflineSubtaskTest : InjectingTestCase() {
    @Inject lateinit var googleTaskDao: GoogleTaskDaoBlocking
    @Inject lateinit var caldavDao: CaldavDaoBlocking
    @Inject lateinit var taskDao: TaskDaoBlocking
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var localBroadcastManager: LocalBroadcastManager

    private lateinit var adapter: TaskAdapter
    private val tasks = ArrayList<TaskContainer>()
    private val filter = BuiltInFilterExposer.getMyTasksFilter(ApplicationProvider.getApplicationContext<Context>().resources)
    private val dataSource = object : TaskAdapterDataSource {
        override fun getItem(position: Int) = tasks[position]

        override fun getTaskCount() = tasks.size
    }

    @Before
    override fun setUp() {
        super.setUp()
        preferences.clear()
        tasks.clear()
        adapter = TaskAdapter(false, googleTaskDao, caldavDao, taskDao, localBroadcastManager)
        adapter.setDataSource(dataSource)
    }

    @Test
    fun singleLevelSubtask() {
        val parent = addTask()
        val child = addTask(with(PARENT, parent))

        query()

        assertEquals(child, tasks[1].id)
        assertEquals(parent, tasks[1].parent)
        assertEquals(1, tasks[1].indent)
    }

    @Test
    fun multiLevelSubtasks() {
        val grandparent = addTask()
        val parent = addTask(with(PARENT, grandparent))
        val child = addTask(with(PARENT, parent))

        query()

        assertEquals(child, tasks[2].id)
        assertEquals(parent, tasks[2].parent)
        assertEquals(2, tasks[2].indent)
    }

    private fun addTask(vararg properties: PropertyValue<in Task?, *>): Long {
        val task = newTask(*properties)
        taskDao.createNew(task)
        return task.id
    }

    private fun query() {
        tasks.addAll(taskDao.fetchTasks { getQuery(preferences, filter, it) })
    }
}