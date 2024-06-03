package com.example.to_do_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.to_do_list.ui.theme.ToDoListTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var cardHeight by remember { mutableStateOf(280.dp) }
            var tasks by remember {
                mutableStateOf(
                    mutableListOf(
                        Task("Task 1", false, 0),
                        Task("Task 2", false, 1),
                        Task("Task 3", false, 2)
                    )
                )
            }
            var showDialog by remember { mutableStateOf(false) }
            var newTaskName by remember { mutableStateOf("") }
            var searchQuery by remember { mutableStateOf("") }
            var filteredTasks by remember { mutableStateOf(tasks.toList()) }

            LaunchedEffect(searchQuery, tasks) {
                filteredTasks = tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            ToDoListTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        ToDoHeader()
                        var active by remember { mutableStateOf(false) }

                        SearchBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            colors = SearchBarDefaults.colors(Color.LightGray),
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { active = false },
                            active = active,
                            onActiveChange = { active = it },
                            placeholder = { Text(text = "Search To Dos") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                            trailingIcon = {
                                if (active) {
                                    Icon(
                                        modifier = Modifier.clickable {
                                            if (searchQuery.isNotEmpty()) {
                                                searchQuery = ""
                                            } else {
                                                active = false
                                            }
                                        },
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Icon"
                                    )
                                }
                            },
                            content = {}
                        )

                        fun increaseCardHeight() {
                            cardHeight *= 1.3f
                        }

                        fun decreaseCardHeight() {
                            if (tasks.isNotEmpty() || tasks.size < 3) {
                                cardHeight /= 1.3f
                            }
                        }

                        TaskScreen(
                            tasks = filteredTasks,
                            cardHeight = cardHeight,
                            onDelete = { index ->
                                tasks = tasks.toMutableList().apply { removeAt(index) }
                                decreaseCardHeight()
                            },
                            onToggleComplete = { index ->
                                tasks = tasks.toMutableList().apply {
                                    val task = get(index)
                                    set(index, task.copy(completed = !task.completed))
                                }
                                tasks = tasks.sortedWith(compareBy<Task> { it.completed }.thenBy { it.originalIndex }).toMutableList()
                                filteredTasks = tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }
                            }
                        )
                    }

                    AddButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        showDialog = true
                    }

                    if (showDialog) {
                        AddTaskDialog(
                            newTaskName = newTaskName,
                            onNewTaskNameChange = { newTaskName = it },
                            onDismiss = { showDialog = false },
                            onAdd = {
                                tasks = tasks.toMutableList().apply {
                                    add(0, Task(newTaskName, false, tasks.size))
                                }
                                tasks = tasks.mapIndexed { index, task -> task.copy(originalIndex = index) }.toMutableList()
                                cardHeight *= 1.3f
                                showDialog = false
                                newTaskName = ""
                                filteredTasks = tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToDoHeader() {
    Text(
        text = "To-dos",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tasks: List<Task>,
    cardHeight: Dp,
    onDelete: (Int) -> Unit,
    onToggleComplete: (Int) -> Unit
) {
    Column {
        TaskList(tasks, cardHeight, onDelete, onToggleComplete)
    }
}

@Composable
fun AddButton(
    modifier: Modifier = Modifier,
    onAdd: () -> Unit) {
    FloatingActionButton(
        onClick = { onAdd() },
        shape = CircleShape,
        containerColor = Color.White,
        modifier = modifier.padding(bottom = 66.dp, end = 16.dp),
        content = {
            Icon(Icons.Filled.Add, contentDescription = "Add Task")
        }
    )
}

@Composable
fun TaskList(
    tasks: List<Task>,
    cardHeight: Dp,
    onDelete: (Int) -> Unit,
    onToggleComplete: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(start = 6.dp, top = 16.dp,end = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                val index = tasks.indexOf(task)
                TaskItem(task, index, onDelete, onToggleComplete)
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    index: Int,
    onDelete: (Int) -> Unit,
    onToggleComplete: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.completed,
            modifier = Modifier.padding(4.dp),
            colors = CheckboxDefaults.colors(checkedColor = Color.LightGray, uncheckedColor = Color.Gray),
            onCheckedChange = { onToggleComplete(index) }
        )

        Text(
            text = task.name,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer { alpha = if (task.completed) 0.5f else 1f }
                .clickable { },
            textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
            color = if (task.completed) Color.Gray else Color.Black
        )
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            modifier = Modifier.clickable { onDelete(index) }
        )
    }
}

@Composable
fun AddTaskDialog(
    newTaskName: String,
    onNewTaskNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        confirmButton = {
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(Color.Transparent, contentColor = Color.Black),

                )
            {
                Text("Add")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(Color.Transparent, contentColor = Color.Black),

                ) {
                Text("Cancel")
            }
        },
        title = {
            Text("Add a to-do item")
        },
        text = {
            OutlinedTextField(
                value = newTaskName,
                onValueChange = onNewTaskNameChange,
            )
        }
    )
}

data class Task(val name: String, val completed: Boolean, val originalIndex: Int)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ToDoListTheme {
        ToDoHeader()
    }
}
