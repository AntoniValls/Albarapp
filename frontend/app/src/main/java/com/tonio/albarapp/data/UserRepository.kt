package com.tonio.albarapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tonio.albarapp.MockUsers
import com.tonio.albarapp.User

object UserRepository {
    private var users = mutableListOf<User>()
    private var preferences: SharedPreferences? = null
    private val gson = Gson()
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USERS = "users"

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPreferences()

        // If no users exist, add the mock users
        if (users.isEmpty()) {
            users.addAll(MockUsers.allUsers)
            saveToPreferences()
        }
    }

    private fun loadFromPreferences() {
        val json = preferences?.getString(KEY_USERS, null)
        if (json != null) {
            val type = object : TypeToken<List<User>>() {}.type
            users = gson.fromJson<List<User>>(json, type).toMutableList()
        }
    }

    private fun saveToPreferences() {
        val json = gson.toJson(users)
        preferences?.edit()?.putString(KEY_USERS, json)?.apply()
    }

    fun getAll(): List<User> = users.toList()

    fun getById(id: String): User? = users.find { it.id == id }

    fun add(user: User) {
        users.add(user)
        saveToPreferences()
    }

    fun update(user: User) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
            saveToPreferences()
        }
    }

    fun delete(userId: String) {
        users.removeAll { it.id == userId }
        saveToPreferences()
    }

    fun getUsersByRole(role: com.tonio.albarapp.UserRole): List<User> {
        return users.filter { it.role == role }
    }
}