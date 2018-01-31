package edu.uoft.ece.tattan

import edu.stanford.nlp.simple.*;
import com.ericsson.otp.erlang.*;

fun main(args: Array<String>) {
    var node: OtpNode? = null

    try {
        node = OtpNode("NLP_NODE")
        node.setCookie("SUPER_SECRET")

        val mailBox = node.createMbox("NLP_MBOX")

        val incomingTuple : OtpErlangTuple
        while (true) {
            val incomingTuple = mailBox.receive() as OtpErlangTuple
            val requestName = incomingTuple.elementAt(0) as OtpErlangAtom
            val requestData = incomingTuple.elementAt(1) as OtpErlangMap

            print(requestName.toString())
            print(requestData.toString())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
       node?.close()

    }
}

