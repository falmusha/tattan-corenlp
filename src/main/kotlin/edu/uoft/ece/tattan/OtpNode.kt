package edu.uoft.ece.tattan

import edu.stanford.nlp.simple.*
import com.ericsson.otp.erlang.*

fun preloadNLPService() {
    val sentence = Sentence("something something")
    sentence.sentiment()
}

fun sentiment(utterance: OtpErlangObject): OtpErlangTuple {
    if (utterance is OtpErlangBinary) {

        val utteranceString = utterance.toString()
        val sentence = Sentence(utteranceString)

        return when (sentence.sentiment()) {
            SentimentClass.VERY_NEGATIVE ->
                OtpErlangTuple(arrayOf(OtpErlangAtom("ok"), OtpErlangAtom("very_negative")))
            SentimentClass.NEGATIVE ->
                OtpErlangTuple(arrayOf(OtpErlangAtom("ok"), OtpErlangAtom("negative")))
            SentimentClass.NEUTRAL ->
                OtpErlangTuple(arrayOf(OtpErlangAtom("ok"), OtpErlangAtom("neutral")))
            SentimentClass.POSITIVE ->
                OtpErlangTuple(arrayOf(OtpErlangAtom("ok"), OtpErlangAtom("positive")))
            SentimentClass.VERY_POSITIVE ->
                OtpErlangTuple(arrayOf(OtpErlangAtom("ok"), OtpErlangAtom("very_positive")))
            else ->
                errorTuple("Not valid utterance")
        }
    } else {
        return errorTuple("Not valid utterance")
    }
}

fun errorTuple(message: String): OtpErlangTuple {
    return OtpErlangTuple(arrayOf(OtpErlangAtom("error"), OtpErlangString(message)))
}

fun receiveMessages(node: OtpNode) {
    val process = node.createMbox("NLP_MBOX")

    println("Process ${process.self()} is running on $node")

    while (true) {
        val incomingTuple = process.receive() as OtpErlangTuple
        val senderPid = incomingTuple.elementAt(0) as OtpErlangPid

        println("Received from: $senderPid --> $incomingTuple")

        val requestName = incomingTuple.elementAt(1)

        if (requestName is OtpErlangAtom && incomingTuple.arity() == 3) {
            val result: OtpErlangObject = when (requestName.atomValue()) {
                "sentiment" -> sentiment(incomingTuple.elementAt(2))
                else -> errorTuple("unknown request name")
            }
            process.send(senderPid, result)
        } else {
            process.send(senderPid, errorTuple("wrong message format, should be {pid, :request, data}"))
        }

    }
}

fun main(args: Array<String>) {
    var node: OtpNode? = null

    while (true) {
        try {
            node = OtpNode("NLP_NODE")
            preloadNLPService()
            receiveMessages(node)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            node?.close()
        }
    }
}

