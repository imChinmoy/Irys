package com.irys.app.domain.usecases.message

import com.irys.app.domain.model.Message
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.repo.ConversationRepository
import com.irys.app.domain.repo.MessageRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        destinationId: String,
        payload: String,
        priority: MessagePriority = MessagePriority.NORMAL
    ): Message {
        val trimmed = payload.trim()
        require(trimmed.isNotEmpty()) { "Message payload cannot be empty" }

        val message = messageRepository.createLocalMessage(
            conversationId = conversationId,
            destinationId = destinationId,
            payload = trimmed,
            priority = priority
        )

        conversationRepository.updateLastMessage(
            conversationId = conversationId,
            lastMessage = trimmed,
            timestamp = message.createdAt,
            status = message.status
        )

        return message
    }
}
