package com.irys.app.domain.usecases.peer

import com.irys.app.domain.repo.PeerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPeerCountUseCase @Inject constructor(
    private val peerRepository: PeerRepository
) {
    operator fun invoke(): Flow<Int> = peerRepository.getPeerCount()
}
