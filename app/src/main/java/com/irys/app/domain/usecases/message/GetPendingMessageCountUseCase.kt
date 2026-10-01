package com.irys.app.domain.usecases.message

import com.irys.app.domain.repo.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingMessageCountUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(): Flow<Int> = messageRepository.getPendingMessageCount()
}
