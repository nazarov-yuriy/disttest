package org.strangeway.disttest

/*
if (description != null) {
    progress(subtasks, this)
} else {
    progress(subtasks)
}
 */
interface Task {
    String getDescription()
    Task[] getSubTasks()
    long getPercentage()
}