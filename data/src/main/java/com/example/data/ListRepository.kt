/*
 * [TAPPSI SAS] LLC ("TAPPSI") CONFIDENTIAL
 * Copyright (c) 2011-2014 [TAPPSI SAS], All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of TAPPSI SAS.
 * The intellectual and technical concepts contained herein are proprietary to
 * TAPPSI SAS and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly
 * forbidden unless prior written permission is obtained from TAPPSI SAS.  Access to
 * the source code contained herein is hereby forbidden to anyone except current
 * TAPPSI SAS employees, managers or contractors who have executed Confidentiality
 * and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication
 * or disclosure  of  this source code, which includes information that is
 * confidential and/or proprietary, and is a trade secret, of  TAPPSI SAS.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT THE EXPRESS
 * WRITTEN CONSENT OF TAPPSI SAS IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF
 * THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY
 * RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE,
 * USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.example.data

import com.example.data.local.LocalStorage
import com.example.data.dao.SubRedditDAO
import com.example.data.remote.RedditService
import com.example.domain.exceptions.OfflineModeException
import com.example.domain.exceptions.UnexpectedHttpCodeException
import com.example.domain.models.SubReddit
import com.example.domain.repository.ListContractModel
import io.reactivex.Observable

class ListRepository(val redditService: RedditService, val localStorage: LocalStorage):
        ListContractModel {

    override fun getRemoteEntries(): Observable<List<SubReddit>> {
        return redditService.getReddits().map { response ->
            when (response.code()) {
                200 -> response.body().data.dataList.map { it.subReddit.toSubReddit() }
                503 -> throw OfflineModeException()
                else -> throw UnexpectedHttpCodeException()
            }
        }
    }

    override fun saveToLocalStorage(list: List<SubReddit>) {
        val daoList = list.map(::SubRedditDAO)
        localStorage.saveObjectList(SubRedditDAO::class.java, daoList)
    }

    override fun clearLocalStorage() {
        localStorage.deleteAllObjects(SubRedditDAO::class.java)
    }

    override fun getLocalEntries(): Observable<List<SubReddit>> {
        val list = localStorage.retrieveAllObjects(SubRedditDAO::class.java).map {
            (it as SubRedditDAO).toSubReddit()
        }
        return Observable.just(list)
    }
}