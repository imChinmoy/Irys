package com.irys.app.domain.usecases.message

import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.repo.ConversationRepository
import com.irys.app.domain.repo.MessageRepository
import javax.inject.Inject

class UpdateMessageStatusUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(messageId: String, status: MessageStatus) {
        messageRepository.updateMessageStatus(messageId, status)
        val message = messageRepository.getMessageById(messageId)
        if (message != null) {
            conversationRepository.updateLastMessage(
                conversationId = message.conversationId,
                lastMessage = message.payload,
                timestamp = message.createdAt,
                status = status
            )
        }
    }
}
