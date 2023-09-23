package com.example.jetpacknotes.navigation

sealed class Screen(
    val route: String
) {

    object BottomNavigation: Screen(ROUTE_BOTTOM_NAVIGATION)
    object NotesList: Screen(ROUTE_NOTES_LIST)
    object NoteEdit: Screen(ROUTE_NOTE_EDIT) {
        private const val ROUTE_FOR_ARGS = "note_edit"
        fun getRouteWithArgs(noteId: Int?) = "$ROUTE_FOR_ARGS/$noteId"
    }
    object RemindersList: Screen(ROUTE_REMINDERS_LIST)
    object TasksList: Screen(ROUTE_TASKS_LIST)

    private companion object {
        const val ROUTE_BOTTOM_NAVIGATION = "bottom_navigation"
        const val ROUTE_NOTES_LIST = "notes_list"
        const val ROUTE_NOTE_EDIT = "note_edit/{id}"
        const val ROUTE_REMINDERS_LIST = "reminders_list"
        const val ROUTE_TASKS_LIST = "tasks_list"
    }
}
