package com.irys.app.domain.usecases.conversation

import com.irys.app.domain.repo.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationCountUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(): Flow<Int> = conversationRepository.getConversationCount()
}
