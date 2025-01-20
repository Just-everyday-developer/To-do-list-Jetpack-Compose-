package com.example.todolist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import android.os.SystemClock.sleep
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.theme.TodoListTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun saveTasks(context: Context, tasks: List<Task>) {
    val sharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val json = Gson().toJson(tasks)
    editor.putString("tasks", json)
    editor.apply()
}

fun loadTasks(context: Context): List<Task> {
    val sharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
    val json = sharedPreferences.getString("tasks", null)
    return if (json != null) {
        val type = object : TypeToken<List<Task>>() {}.type
        Gson().fromJson(json, type)
    } else {
        emptyList()
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoListTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Board(Modifier.padding(innerPadding).fillMaxSize(), this@MainActivity)
                }
            }
        }
    }
}

data class Task(
    val id: Int,
    val text: String
)

@Composable
fun Board(modifier: Modifier, context: Context) {
    var tasks by remember { mutableStateOf(loadTasks(context)) }
    var nextId by remember { mutableStateOf(tasks.maxOfOrNull { it.id + 1 } ?: 0) }
    var inputText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Title(40.dp)
        Navigation_Bar(
            inputText = inputText,
            onInputChange = { inputText = it },
            onAddTask = {
                if (inputText.isNotBlank()) {
                    tasks = tasks + Task(nextId, inputText)
                    tasks = tasks.sortedBy { it.text }
                    nextId++
                    inputText = ""
                    saveTasks(context, tasks)
                }
            }
        )
        List_Of_Tasks(
            tasks = tasks,
            onDeleteTask = { taskId ->
                tasks = tasks.filterNot { it.id == taskId }
                saveTasks(context, tasks)
            }
        )
    }
}


@Composable
fun Title(padding: Dp) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "My Simple Todo List",
                fontWeight = FontWeight.W700,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                color = Color.Yellow
            )
        }
    }
}

@Composable
fun Input_Text_Field(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
) {
    OutlinedTextField(
        value = text,
        shape = RoundedCornerShape(CornerSize(25.dp)),
        singleLine = true,
        onValueChange = onTextChange,
        label = { Text("Enter a task", style = TextStyle(color = Color.White, fontSize = 20.sp)) },
        textStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.W500,
            fontFamily = FontFamily.SansSerif,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Red, Color(0xFFFFA500), Color.Yellow, Color.Green, Color.Blue, Color(0xFF4B0082), Color(0xFF7F00FF)
                )
            )
        ),
        visualTransformation = VisualTransformation.None,
        modifier = modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(colors = listOf(Color.LightGray, Color.Gray)),
            shape = RoundedCornerShape(CornerSize(25.dp))
        )
    )
}

@Composable
fun Add(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier.size(width = 200.dp, height = 200.dp).fillMaxWidth()
) {
    var text by remember { mutableStateOf("Add") }
    var color by remember { mutableStateOf(Color.Red) }
    val scope = rememberCoroutineScope()

    FilledTonalButton(
        modifier = Modifier.padding(top = 20.dp).size(width = 200.dp, height = 50.dp),
        enabled = true,
        shape = RoundedCornerShape(CornerSize(10.dp)),
        colors = ButtonDefaults.buttonColors(color),
        elevation = ButtonDefaults.buttonElevation(10.dp),
        onClick = {
            scope.launch {
                color = Color.Green
                text = "Added"
                onAddClick()
                delay(1000)
                color = Color.Red
                text = "Add"
            }
        }
    ) {
        Text(text, fontSize = 25.sp, fontWeight = FontWeight.W600, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun Navigation_Bar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onAddTask: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Input_Text_Field(
            text = inputText,
            onTextChange = onInputChange
        )
        Add(onAddClick = onAddTask)
    }
}

@Composable
fun List_Of_Tasks(
    tasks: List<Task>,
    onDeleteTask: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Tasks",
            fontSize = 32.sp,
            fontWeight = FontWeight.W600,
            fontFamily = FontFamily.SansSerif,
            color = Color.Blue,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(400.dp), // Fixed height for scrollable area
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = tasks.size,
                    key = { index -> tasks[index].id }
                ) { index ->
                    TaskItem(
                        task = tasks[index],
                        onDelete = { onDeleteTask(tasks[index].id) }
                    )
                }

                // Add empty item at the bottom for better padding
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.text,
                fontSize = 18.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = Color.Red
                )
            }
        }
    }
}



