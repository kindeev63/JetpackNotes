package com.example.jetpacknotes.navigation

sealed class Screen(
    val route: String
) {

    object Notes: Screen(ROUTE_NOTES)
    object NotesList: Screen(ROUTE_NOTES_LIST)
    object NoteEdit: Screen(ROUTE_NOTE_EDIT) {
        private const val ROUTE_FOR_ARGS = "note_edit"
        fun getRouteWithArgs(noteId: Int) = "$ROUTE_FOR_ARGS/$noteId"
    }
    object Reminders: Screen(ROUTE_REMINDERS)
    object Tasks: Screen(ROUTE_TASKS)

    private companion object {
        const val ROUTE_NOTES = "notes"
        const val ROUTE_NOTES_LIST = "notes_list"
        const val ROUTE_NOTE_EDIT = "note_edit/{noteId}"
        const val ROUTE_REMINDERS = "reminders"
        const val ROUTE_TASKS = "tasks"
    }
}
