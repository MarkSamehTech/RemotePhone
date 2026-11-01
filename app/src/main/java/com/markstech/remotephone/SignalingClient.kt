package com.markstech.remotephone

import com.google.firebase.database.*
import kotlin.random.Random

/**
 * إشارة الاقتران والتفاوض (Offer/Answer/ICE) عبر Firebase Realtime Database.
 * كل جلسة لها كود من 6 أرقام (room code) يُنشئه الجهاز "المضيف" (اللي هيتم التحكم فيه)
 * والجهاز "المتحكِّم" بيدخل نفس الكود للانضمام للغرفة.
 */
class SignalingClient(private val roomCode: String) {

    private val db: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("rooms").child(roomCode)

    companion object {
        fun generateRoomCode(): String =
            (100000..999999).random().toString()
    }

    fun sendOffer(sdp: String) {
        db.child("offer").setValue(sdp)
    }

    fun sendAnswer(sdp: String) {
        db.child("answer").setValue(sdp)
    }

    fun sendIceCandidate(role: String, candidate: String) {
        db.child("candidates").child(role).push().setValue(candidate)
    }

    fun listenForOffer(onOffer: (String) -> Unit) {
        db.child("offer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(String::class.java)?.let(onOffer)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun listenForAnswer(onAnswer: (String) -> Unit) {
        db.child("answer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(String::class.java)?.let(onAnswer)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun listenForIceCandidates(remoteRole: String, onCandidate: (String) -> Unit) {
        db.child("candidates").child(remoteRole)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(String::class.java)?.let(onCandidate)
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /** تنظيف الغرفة بعد انتهاء الجلسة */
    fun closeRoom() {
        db.removeValue()
    }
}
