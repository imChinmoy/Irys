package com.irys.app.domain.usecases.message

import com.irys.app.domain.model.Message
import com.irys.app.domain.repo.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesForConversationUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(conversationId: String): Flow<List<Message>> {
        return messageRepository.getMessagesForConversation(conversationId)
    }
}
