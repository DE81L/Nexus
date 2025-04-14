package com.example.nexus.chatapp.request

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.chat.ChatActivity
import com.example.nexus.chatapp.databinding.ActivityRequestDetailBinding
import com.example.nexus.chatapp.model.Comment
import com.example.nexus.chatapp.model.Request
import com.example.nexus.chatapp.model.RequestStatus
import com.example.nexus.chatapp.model.User
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.LocalStorage
import com.example.nexus.chatapp.utils.ThemeManager
import com.example.nexus.chatapp.utils.formatDateTime
import com.example.nexus.chatapp.utils.hideKeyboard
import com.example.nexus.chatapp.utils.showAlert
import com.example.nexus.chatapp.utils.showToast
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class RequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestDetailBinding
    private lateinit var localStorage: LocalStorage
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager
    
    private lateinit var currentUser: User
    private var currentUserId: String = ""
    private var requestId: String = ""
    private var isNewRequest: Boolean = false
    private var completionDate: Long? = null
    private var selectedAssigneeId: String = ""
    private lateinit var users: List<User>
    private lateinit var commentAdapter: CommentAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize managers
        themeManager = ThemeManager(this)
        languageManager = LanguageManager(this)
        localStorage = LocalStorage(this)
        
        // Apply theme and language
        themeManager.applyThemeForActivity(this)
        languageManager.applyLanguageForActivity(this)
        
        // Use view binding
        binding = ActivityRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get current user ID
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("current_user_id", "") ?: ""
        
        if (currentUserId.isEmpty()) {
            finish()
            return
        }
        
        // Get request ID or check if it's a new request
        requestId = intent.getStringExtra("REQUEST_ID") ?: ""
        isNewRequest = intent.getBooleanExtra("IS_NEW_REQUEST", false)
        
        if (!isNewRequest && requestId.isEmpty()) {
            finish()
            return
        }
        
        setupForEditOrView()
        setupListeners()
        setupCommentRecyclerView()
        loadCurrentUser()
        loadUsers()
        
        if (!isNewRequest) {
            loadRequest()
        }
    }
    
    private fun setupForEditOrView() {
        if (isNewRequest) {
            supportActionBar?.title = getString(R.string.new_request)
            binding.buttonContainer.visibility = View.VISIBLE
            binding.buttonSave.setText(R.string.create)
            binding.editableFieldsContainer.visibility = View.VISIBLE
            binding.viewOnlyFieldsContainer.visibility = View.GONE
            binding.spinnerAssignee.visibility = View.VISIBLE
            binding.editTaskDescription.visibility = View.VISIBLE
            binding.editRoomNumber.visibility = View.VISIBLE
            binding.buttonSetCompletionDate.visibility = View.VISIBLE
            binding.buttonRemoveCompletionDate.visibility = View.VISIBLE
            binding.commentsSection.visibility = View.GONE
        } else {
            supportActionBar?.title = getString(R.string.request_details)
            binding.editableFieldsContainer.visibility = View.GONE
            binding.viewOnlyFieldsContainer.visibility = View.VISIBLE
        }
    }
    
    private fun setupListeners() {
        // Set completion date button
        binding.buttonSetCompletionDate.setOnClickListener {
            showDatePicker()
        }
        
        // Remove completion date button
        binding.buttonRemoveCompletionDate.setOnClickListener {
            completionDate = null
            binding.textCompletionDate.text = getString(R.string.not_set)
            binding.buttonRemoveCompletionDate.visibility = View.GONE
        }
        
        // Save button for new request
        binding.buttonSave.setOnClickListener {
            if (isNewRequest) {
                createNewRequest()
            } else {
                updateRequestStatus()
            }
        }
        
        // Cancel button
        binding.buttonCancel.setOnClickListener {
            finish()
        }
        
        // Send comment button
        binding.buttonSendComment.setOnClickListener {
            addComment()
        }
        
        // Chat button
        binding.buttonChat.setOnClickListener {
            lifecycleScope.launch {
                val request = localStorage.getRequestById(requestId)
                if (request != null) {
                    // Determine who to chat with based on the current user
                    val chatUserId = if (currentUserId == request.requesterId) {
                        request.assigneeId
                    } else {
                        request.requesterId
                    }
                    
                    val chatUserName = if (currentUserId == request.requesterId) {
                        request.assigneeName
                    } else {
                        request.requesterName
                    }
                    
                    val intent = Intent(this@RequestDetailActivity, ChatActivity::class.java).apply {
                        putExtra("CHAT_USER_ID", chatUserId)
                        putExtra("CHAT_USER_NAME", chatUserName)
                    }
                    startActivity(intent)
                }
            }
        }
    }
    
    private fun setupCommentRecyclerView() {
        commentAdapter = CommentAdapter()
        binding.recyclerComments.layoutManager = LinearLayoutManager(this)
        binding.recyclerComments.adapter = commentAdapter
    }
    
    private fun loadCurrentUser() {
        lifecycleScope.launch {
            val user = localStorage.getUserById(currentUserId)
            if (user != null) {
                currentUser = user
            } else {
                finish()
            }
        }
    }
    
    private fun loadUsers() {
        lifecycleScope.launch {
            users = localStorage.getUsers().filter { it.id != currentUserId }
            
            if (isNewRequest) {
                // Setup assignee spinner for new request
                val userNames = users.map { it.fullName }
                val adapter = ArrayAdapter(
                    this@RequestDetailActivity,
                    android.R.layout.simple_spinner_item,
                    userNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerAssignee.adapter = adapter
                
                binding.spinnerAssignee.setOnItemSelectedListener { _, _, position, _ ->
                    selectedAssigneeId = users[position].id
                }
            }
        }
    }
    
    private fun loadRequest() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val request = localStorage.getRequestById(requestId)
            
            binding.progressBar.visibility = View.GONE
            
            if (request == null) {
                showToast(getString(R.string.request_not_found))
                finish()
                return@launch
            }
            
            // Display request details
            binding.textTaskDescription.text = request.taskDescription
            binding.textRoomNumber.text = request.roomNumber
            binding.textRequestDate.text = request.getFormattedRequestDate()
            
            if (request.completionDate != null) {
                binding.textCompletionDateView.text = request.getFormattedCompletionDate()
                binding.textCompletionDateView.visibility = View.VISIBLE
                binding.labelCompletionDateView.visibility = View.VISIBLE
            } else {
                binding.textCompletionDateView.visibility = View.GONE
                binding.labelCompletionDateView.visibility = View.GONE
            }
            
            // Set requester and assignee information
            binding.textRequesterName.text = request.requesterName
            binding.textAssigneeName.text = request.assigneeName
            
            // Set status
            when (request.status) {
                RequestStatus.PENDING -> binding.textStatus.setText(R.string.status_pending)
                RequestStatus.IN_PROGRESS -> binding.textStatus.setText(R.string.status_in_progress)
                RequestStatus.COMPLETED -> binding.textStatus.setText(R.string.status_completed)
                RequestStatus.CANCELLED -> binding.textStatus.setText(R.string.status_cancelled)
            }
            
            // Show buttons based on current status and user role
            setupStatusButtons(request)
            
            // Load comments
            commentAdapter.submitList(request.comments)
            
            if (request.comments.isEmpty()) {
                binding.textNoComments.visibility = View.VISIBLE
            } else {
                binding.textNoComments.visibility = View.GONE
            }
        }
    }
    
    private fun setupStatusButtons(request: Request) {
        // Clear existing buttons
        binding.buttonContainer.visibility = View.VISIBLE
        binding.buttonSave.visibility = View.VISIBLE
        binding.buttonCancel.visibility = View.GONE
        
        // Set button text and action based on user role and request status
        if (currentUserId == request.requesterId) {
            // Current user is the requester
            when (request.status) {
                RequestStatus.PENDING -> {
                    binding.buttonSave.setText(R.string.cancel_request)
                }
                RequestStatus.IN_PROGRESS -> {
                    binding.buttonSave.setText(R.string.mark_as_completed)
                }
                RequestStatus.COMPLETED, RequestStatus.CANCELLED -> {
                    binding.buttonContainer.visibility = View.GONE
                }
            }
        } else {
            // Current user is the assignee
            when (request.status) {
                RequestStatus.PENDING -> {
                    binding.buttonSave.setText(R.string.accept_request)
                }
                RequestStatus.IN_PROGRESS -> {
                    binding.buttonSave.setText(R.string.mark_as_completed)
                }
                RequestStatus.COMPLETED, RequestStatus.CANCELLED -> {
                    binding.buttonContainer.visibility = View.GONE
                }
            }
        }
    }
    
    private fun updateRequestStatus() {
        lifecycleScope.launch {
            val request = localStorage.getRequestById(requestId)
            
            if (request == null) {
                showToast(getString(R.string.request_not_found))
                return@launch
            }
            
            // Determine new status based on user role and current status
            val newStatus = if (currentUserId == request.requesterId) {
                // Current user is the requester
                when (request.status) {
                    RequestStatus.PENDING -> RequestStatus.CANCELLED
                    RequestStatus.IN_PROGRESS -> RequestStatus.COMPLETED
                    else -> request.status // No change
                }
            } else {
                // Current user is the assignee
                when (request.status) {
                    RequestStatus.PENDING -> RequestStatus.IN_PROGRESS
                    RequestStatus.IN_PROGRESS -> RequestStatus.COMPLETED
                    else -> request.status // No change
                }
            }
            
            // Update only if there's a change
            if (newStatus != request.status) {
                val updatedRequest = request.copy(status = newStatus)
                localStorage.updateRequest(updatedRequest)
                
                // Add a system comment about the status change
                val statusChangeComment = Comment(
                    id = localStorage.generateId(),
                    userId = currentUserId,
                    userName = currentUser.fullName,
                    timestamp = System.currentTimeMillis(),
                    text = when (newStatus) {
                        RequestStatus.IN_PROGRESS -> getString(R.string.comment_request_accepted)
                        RequestStatus.COMPLETED -> getString(R.string.comment_request_completed)
                        RequestStatus.CANCELLED -> getString(R.string.comment_request_cancelled)
                        else -> "" // Should not happen
                    }
                )
                
                if (statusChangeComment.text.isNotEmpty()) {
                    localStorage.addCommentToRequest(requestId, statusChangeComment)
                }
                
                loadRequest() // Refresh the view
            }
        }
    }
    
    private fun createNewRequest() {
        val taskDescription = binding.editTaskDescription.text.toString().trim()
        val roomNumber = binding.editRoomNumber.text.toString().trim()
        
        // Validate input
        if (taskDescription.isEmpty()) {
            showToast(getString(R.string.enter_task_description))
            return
        }
        
        if (roomNumber.isEmpty()) {
            showToast(getString(R.string.enter_room_number))
            return
        }
        
        if (selectedAssigneeId.isEmpty() && users.isNotEmpty()) {
            selectedAssigneeId = users[0].id
        }
        
        if (selectedAssigneeId.isEmpty()) {
            showToast(getString(R.string.no_assignee_selected))
            return
        }
        
        lifecycleScope.launch {
            val assignee = localStorage.getUserById(selectedAssigneeId)
            
            if (assignee == null) {
                showToast(getString(R.string.assignee_not_found))
                return@launch
            }
            
            // Create the request
            val newRequest = Request(
                id = localStorage.generateId(),
                requesterId = currentUserId,
                requesterName = currentUser.fullName,
                assigneeId = assignee.id,
                assigneeName = assignee.fullName,
                requestDate = System.currentTimeMillis(),
                completionDate = completionDate,
                taskDescription = taskDescription,
                roomNumber = roomNumber,
                status = RequestStatus.PENDING
            )
            
            localStorage.addRequest(newRequest)
            
            showToast(getString(R.string.request_created))
            finish()
        }
    }
    
    private fun addComment() {
        val commentText = binding.editComment.text.toString().trim()
        
        if (commentText.isEmpty()) {
            return
        }
        
        lifecycleScope.launch {
            val comment = Comment(
                id = localStorage.generateId(),
                userId = currentUserId,
                userName = currentUser.fullName,
                timestamp = System.currentTimeMillis(),
                text = commentText
            )
            
            localStorage.addCommentToRequest(requestId, comment)
            
            binding.editComment.text?.clear()
            hideKeyboard()
            
            loadRequest() // Refresh to show new comment
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        if (completionDate != null) {
            calendar.timeInMillis = completionDate!!
        }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                completionDate = calendar.timeInMillis
                binding.textCompletionDate.text = completionDate!!.formatDateTime("dd MMM yyyy")
                binding.buttonRemoveCompletionDate.visibility = View.VISIBLE
            },
            year,
            month,
            day
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    inner class CommentAdapter : 
        androidx.recyclerview.widget.ListAdapter<Comment, CommentAdapter.CommentViewHolder>(
            object : DiffUtil.ItemCallback<Comment>() {
                override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                    return oldItem.id == newItem.id
                }
                
                override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                    return oldItem == newItem
                }
            }
        ) {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
        
        inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val userNameText: android.widget.TextView = itemView.findViewById(R.id.textUserName)
            private val commentText: android.widget.TextView = itemView.findViewById(R.id.textComment)
            private val timeText: android.widget.TextView = itemView.findViewById(R.id.textTime)
            
            fun bind(comment: Comment) {
                userNameText.text = comment.userName
                commentText.text = comment.text
                timeText.text = comment.getFormattedTime()
            }
        }
    }
}
