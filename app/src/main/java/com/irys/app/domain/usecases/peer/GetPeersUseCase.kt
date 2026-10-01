package com.irys.app.domain.usecases.peer

import com.irys.app.domain.model.Peer
import com.irys.app.domain.repo.PeerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPeersUseCase @Inject constructor(
    private val peerRepository: PeerRepository
) {
    operator fun invoke(): Flow<List<Peer>> = peerRepository.getPeers()
}
