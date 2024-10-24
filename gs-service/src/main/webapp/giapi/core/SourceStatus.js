/**
 * @module Core
 **/

/**
 * This class represents the status of a {{#crossLink "DABSource"}}DAB source{{/crossLink}} during a
 *  {{#crossLink "DAB/discover:method"}}{{/crossLink}}
 *
 * @class SourceStatus
 **/
GIAPI.SourceStatus = function(status) {
    
    return {

        /**
         * Retrieves the {{#crossLink "Report/id:property"}}report id{{/crossLink}} of this {{#crossLink "DABSource"}}source{{/crossLink}}
         *
         * @method id
         * @return {String} A String with the {{#crossLink "Report/id:property"}}report id{{/crossLink}} of this {{#crossLink "DABSource"}}source{{/crossLink}}
         */
        id : function() {

            return status.id;
        },

        /**
         * Retrieves the {{#crossLink "Report/title:property"}}report title{{/crossLink}} of this {{#crossLink "DABSource"}}source{{/crossLink}}
         *
         * @method title
         * @return {String} A String with the {{#crossLink "Report/title:property"}}report title{{/crossLink}} of this {{#crossLink "DABSource"}}source{{/crossLink}}
         */
        title : function() {

            return status.title;
        },

        /**
         * Retrieves the percentage of completion of this {{#crossLink "DABSource"}}source{{/crossLink}}
         *
         * @method progress
         * @return {Integer} The percentage of completion of this {{#crossLink "DABSource"}}source{{/crossLink}}
         */
        progress : function() {

            return parseInt(status.progress);
        },

        /**
         * Retrieves a message describing the current activity of this {{#crossLink "DABSource"}}source{{/crossLink}}
         *
         * @method message
         * @return {String} A message describing the current activity of this {{#crossLink "DABSource"}}source{{/crossLink}}
         */
        message : function() {

            return status.message;
        },

        /**
         * Retrieves the name of phase of completion (default) or a data URL of a correspondent predefined icon
         *
         * @method phase
         * @param {Boolean} [dataURL=false] If omitted or set to <code>false</code> returns the name of the phase of completion,
         * otherwise returns the data URL of a correspondent predefined icon as depicted in the following table:
         * <table>
         *     <body>
         *        <tr>
         *           <th>Phase description</th>
         *           <th>Phase name (<code>dataURL</code> omitted or set to <code>false</code>)</th>
         *           <th>Phase as data URL (<code>dataURL</code> set to <code>true</code>)</th>
         *           <th>Phase data URL rendered</th>
         *        </tr>
         *        <tr>
         *           <td>Query initialization</td>
         *           <td>"INIT"</td>
         *           <td>{{#crossLink "SourceStatus/INIT_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADKUlEQVQ4T21TW0gUURj+zszszM7OurvOupar5m4rrZZam21WFoViFBQ9BN2oMJIosBChQopKeunNhx6KiIQigi4PvkhFdyGDoqCLBLJpdHHV3JtuOe7OTGemFKEGPv5z+b/vnPPP9xP856s4UB0o9gXqJpi0j2MJES1ifCQSffrqfM9Lmq7PppDZk+rDdYFAme8sxzM7oWjgRQFE16FaNaiiDk7lXvz8nG69dfRq7zRvRqDh9NZl3kLPAyYNp9sjwyZLIBzd5mgqCyhZBUPDUVhcApQfmUPX9l64aIiYAsGm5f4loco3OWnBWRAoAHgClagYy8SRVJNIqCnEtDh+YRKZEQU1vjBG+5Ibu9tu3DUF1p7ZdLWAce8pDQbMEyem0ljpDWNZUQgsy+JjvB+XI9fQm6IlyABSworykoVfunZc95M5e4L+ed7CT7XF1bDl2RFNRNFYtRP5cj4YhoHFYjFhjFt7T+DZEH2+oiOf8YD9zm4jlUdW7RM14cr6mnWIxAYRkiuwoiQMQgisVitEUTSjgeHJUWzu2gE9q4NVGMgWz0VS09bQLmr8qQpfEB+H+7ElsAF5nGwSjZMlSYLD4TCjMd/bfRCR+AD0KcBpz+0h4eP17XaIp1xSDlKZcazODcPN5cJms8Fut5tEA4Ygz/NofnQMA8nPQBZUWO4hVS1rWtiE2iHn50KnBfSzRShzlM4QDaFpcjI7juanxwCNVn5Sx1zZe4nM2bVgvtvmisicA0Ri8G30G5oqdkO0/Hm/AUEQzHjhbSdej70FVJ2uichj8prM31jZsvqJ8im1Vi52o38kAqeYgw3z67HIEzSvHcsmcP/rE7yL9ZlG1tIq5gaLku9PPveYAt79i0OyVXqsDKacMS5JvUCoCw0H0m3qQmLEv57VMxpcXjf4jLC9r+PFzRkrLzq8olFX9M7RgSGoFg1EMMhUiKFCBgzbKoDL74FV59vedfSem7HydGOUHqhe6nI4O7kJVE39VPArOwmd0WCl9ZBcdmRy8GUinm7tO//89j/NNLsrQ0fW1JeXlDW4nXKtwPFkbDz24fuP6MN77Xfu0Dx1du5vpjUHBLzx3/8AAAAASUVORK5CYII="/></td>
         *        </tr>       
         *        <tr>
         *           <td>Distribution of the query to the sources</td>
         *           <td>"DISTRIBUTION"</td>
         *           <td>{{#crossLink "SourceStatus/DISTRIBUTION_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAOCAYAAAAmL5yKAAACPElEQVQoU2NkwA44GTyLznKr+mh+vb3lOsP2PmOgsu/YlDJi1a9u0cHr2VX+9+8vBmZmNobP28s6GW6eqCDeAIeU83yGCQZ//vxgYGHhYPh0fsEFhgNzDIk3QMWwkd9vSt2fPz+BBrAzfNyU08Rw53w98QaAVGrZ9LLblRf9PNTZx3DtSDGOsGJADgNOBnZ2GaDC/ww/mX4w8HBaMPq3rv6/sTqU4cv3Ewzs/ziAcowMP38+QQ5QmAFiDEFNB1kEVDT+/fn1H2wbEzMDO5cI489vb/4z/PsLEWJhY/zz4c4NhnV19kDuK5AYxAA2Hi2mqGln/63O9QGL/f37m4GbT4/Ju3nSv621eQxfP10CRgcryHVMoZO3/FuWZczw68s1mAGiDFp2dex2ZZk/t1eWMTy83Ac2VMO2m92hrOTnga4ehhuHS8Fi8rpF7J7tXT8PdU1nuHaoCSjympHBo/gOj7qv8v///8Bx/mllQgoDG6cIf9CMDngsrMuoYPj1/Q1f+II5oLTByMjE8OXm5rsMO3pVGLnz9v0HaQYBcJyfmLCXgZmDn880wwSeDk7POMPw98dHPosCZ5AY2OlAQ75OcmJkZPCrvcUpa6PKCAw0JiYWhi9rMgsZOLn5eX36GuApcUtRA8P3rx95Qqb3//v3h+E/MFC/Pz5ym2FTsxojA4+AA4NT7ixuBUfVr1dX3WTYO8MEaMEvYF64gJQXDEBBzeCccYZbO0z964P9txn2TU5j+PLhAPa8AHYkcQAAFZXhD6YckjcAAAAASUVORK5CYII="/></td>
         *        </tr>
         *         <tr>
         *           <td>Query validation</td>
         *           <td>"VALIDATION"</td>
         *           <td>{{#crossLink "SourceStatus/VALIDATION_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC7ElEQVQ4T3WTW0iTYRjHn+/TzePmZO7g3GDTqbPZSIiwwFJsmXgjhV1kB2WywC7ydBGTaCYkCFmYSexGM2jEiLTw0FLTmlB5SNfKabVWqJjbch72be5z3/oUFU194b15eP6/9/nz/F8E9j8ihUIRK5PJWDqd7qfBYDCRra7/25G99BUVFZVKpbJSJBIRXq/X7na7AzUaTZ9KpSoh++3bNbsAWq02Jzs7R+f3Ew4URbCgoCDwA1BmZ/9w6+ruaO/X1xeTgJVNyC6AxWLpCg+nSX0+AgsOpgLuI5huzB2J4zhqNI5h6pu3Mo3Gkff7Avr6+mcEAsEShUIJRQMCWCgaEEQQPsDxVXC5lqG69t7nJFHScbW61LkG2TWBXv+6jcvlpgVSqJF0Og1QBAUvjoNnxQO/rFYwmi3g8nhGgUJkqEtLnTsA11U3CpaXFu9eyD/PoNHpsOYfQRDAMAzIiaCzSw9H09LBPDEBjY2NbR/f9eRuAcqrbjd75x2Xi4uvQsfLVhCL4+CkXL5udcHphNa2l5AolUFyshQmJyahqqYGTsizyzYBCJvNbr5YeOUSmxECkgNSWFhcBBaLDaQVWCX8wOXx1yeiUqkwNDgI7R2dlt7erjPbLfAThOyhaGYEJ16aAlMOD6yQvosURSDPOg0+ggC73QFfx8fhQUN9y9sevYocbmYLIOaFprCYUSMxvGg4nJkHNvtfmJ6eAgGLgLyMYHj8JgRGTd9s9jmbatw4/IQUY1tbEDKAERYR8UmSKBF+GB57deqsIosXzQIpxwpCLgqmuXgYMzu7mx5pyl3z819IoW9HDiQxYU0JEmmBY/pHy4DZUcHhcBqulZWcOygi4PscAzP0G6qf6bQPSdH67ndEOYEXmhvDFzynUf2WF4bJtDVf5GUfST02kF+odNfWqJXTVuswWcP3+jfIIXHUfFyskDphHs81/XZ1k01k9NcDxt/IvG2jtpcekNRkYTqTivHbR+ae7vfKnsqN4j/7KSLQfZStLgAAAABJRU5ErkJggg=="/></td>
         *        </tr>
         *        <tr>
         *           <td>Query decoding</td>
         *           <td>"DECODING_REQUEST"</td>
         *           <td>{{#crossLink "SourceStatus/DECODING_REQUEST_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADU0lEQVQ4T22TWUxTaRTH/1/pxZaW1lhaKAWxVqTCWJUiIHGZ+OCSkWjc4hKNOiSExAeXmJi4jGZi4gtGo4k+gBHimBlEfZghmnGJ0QdFlGiMKIyOUlahLN3uvvj1jhIS5yYnN/fec373/P/nOwT/cwWXH/JNzw+slSXVoRGFMEaDNNj3T8uTv0610XRtcgmZ/FCx+qivqHjpr6mmtK2aEkEKEaEqCaiwQuDHkvdQZPRz1a26vXe/1U0ANlQ3luT55t3T1LDdleGC2TIVmiJCYkdAGBM4NoreUAeMTBp4Tqq5XLv7UhKiA4I/HswoX7H9vYWJ2nO8cwFVgiSK8Nr6URbMQ921p3jWBbDxGPh4P4r8LgwMRze3XNl3XQds2veoMdtt3pGfXwBVlTEUZvF5OI4ti1kEfsgFy7I4feEB2t7KUGQWROnDLJ+vp+lStZd4/DsdZatqwiWBDKTbsxDqZxGJ0ojEkBh6i7NHS8EwBnAch9qLD/G8U4AisbBaxqlEy8+kYl3thuyZZc3Ll5VgoG8ABnEQiQSH8EgcCvXAbYth+8YkhFAIj/P1j9DexVOJ3bBZ3VdJZc2dk85M/3G/PwuFtnYEF3ihaRoIIVSOCkmSIAgiDR48L9DgcaL2PgZHOJhMpsfkp+o7J7Ny5hx3OF2o8LRhbmEOLf7P3yRAlmUdIorJYgFjo1GcqXtKATymmFIfk2Ubz25y5JY3efICUOL/IjHWQ42i86dmaqoIp3kcO7eUUoCoR/3vL/DqvUjldMFhn3GNzF64f2ZB+a4PTpcFTOo0dHfHKCChu53Cv8PhPbkwmY20CxmX/3iJ1x+hmyiJIUy1ZVXpza6uuv9byhRmW6anCH291LyvgDWBDhTPz9OlNDR34E0oVQez0U46sczIUO+gVwcku/CX726HxtoF1Q31K2Ce8wUqV85G481OdPSm0/csuFgPnYgGo8FY9fzvX+onjvKSdWdKbZ6ld0eHO23mdD9dGQ0SNwp+vBtGawYUMQ4+8YE6n0a/kYZnLYd2TRzlb4sRrLxQ7M4uOGdkhMWKIoMTwrR9ESmKQv8KSHxaT3Ss/0Dr7WPN3y3T5K1ctOZIaWFg4fpMh32+gcAaiYy0fvoUav2z4cANmqdMzv0C2tGZc8nM+tEAAAAASUVORK5CYII="/></td>
         *        </tr>
         *        <tr>
         *           <td>Waiting response from the source</td>
         *           <td>"NETWORK_WAITING"</td>
         *           <td>{{#crossLink "SourceStatus/NETWORK_WAITING_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAACMUlEQVQ4T62TS2gTURSG/5kkk2RaCUkfxle1rRpcxNrUVUHBRYoUuqqbikXBRQhJC4IogkIFxY0ItonRUsSFKEgw1AdCKyJIfay0Dx/VvKTJpBgnsTFpMpnMxLFilEljNt7Vvedyv/vzcQ4B2bLb7QMkSfbI6/KzKIr3PR7PCCG/cDgcEy6Xy1oN4HQ6J91ud1dFgNE6BEKhKuMoaQMW7tpRFbC++wKUdGMZgK5V48ONQ/8RsIo0i8lkqnvyJgwoqFIChtMhlmlAWYJq0pjkMr7lebz7/AmpbA5+hsRBSz2ujgz/kvgbIJdGSHr3d+3AmaNWEEIei3E/WJ5GRNiM2dfvwUyMBn23rreWAHJpbWY9Bg4U8YWdAa22gKIaEY5HEMl2IMlp4H/2Ev6H5/srAo70taNzewDTwTvQUM1AQYf5UAzh7GFo6Rok56X91EVvRcDxwR7oVD7Mvh1HQVBAS2gR5feAFbshxFkkAimwfl+sBGjqHcMaPV2yvq9zNyBeQTTyCDRlQOE7BcF4GlBvQ2KGgZgnpFrwD0DeMcMPQnj8MYRM9CSUMKBGdwoc34J0QpAScWjbWYuvwRde4u8+MJvNepvNJn0NvJoO4MQ1AkVyCXzOC7WqHdk0CZFYwKbmjWjasgupOV//yiys1gtFqX773hwujWpQFBoA/VOQCgdyywzqDYPYatx7duxc79AK4F8jvMTXrQtGOwoZjlpbUB9DejExtcHQN/588vLNn29/AOPlFct62uwRAAAAAElFTkSuQmCC"/></td>
         *        </tr>
         *        <tr>
         *           <td>Collecting sources responses</td>
         *           <td>"JOIN"</td>
         *           <td>{{#crossLink "SourceStatus/JOIN_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAOCAYAAAAmL5yKAAAB/0lEQVQoU6WTS2jUUBSGv3QycdpJE4Xqyo2PqvVFtV2ICxVFtOiiCONrUQQf4EawvgpCrV3VVwU3gg+QLlQcEBdKFVHQhbhotfiqtlU3rqQLEzPtNJNpPMk041AqFEzu5ZI/53z33D8nCv95KWG+zobKbVxTF1LtvuFLppt6UV29kb74MmpyH+l3HlArmpZsoEdbzWJviMHfjzgUAip3MaDOo5oyUGT+6uJoWQLT2E2b74mmgn2XtvEs1swmLvvjkiTT+85gCJh1Gl/u8FLiMPKcZ5Jklq+j3s8VtNGX9AjMqtjIpkArBIdDTtDIkLacBb5Agt2smxxQNKqMJjoigN1Fi+8ybO7nRliVZLof+FrwAGbHV9Ca3MJh5z4nvW90BqJALyS3cjzzmIsSfCLQ1Pk06zs4n3nC1dx72iOARLPUOEivfYvtYXF5ciRZaaa4YqU5QoZ3xJDD4Bv7eGhfpw6XT38BMEffw4tYFUuk7IIjMRk6St6R53zRIyU/zGfnDutF+VkKCCLKmcHcYBfGyGKyxthJ2r5HCovX8i4RVjfGD1lHJ3yccGGKRXrgkt5As9NNp/TCsakiJ1dQjFEXcdbYS2vxK9ym3RvgzGTIPwEVm3mbWEttBMi+om/kKaumDdBq6NBTnIo60Ulzzu2nZdqAwFBpsN6Sf6EuMq4U8gdpTqhx8Av8wQAAAABJRU5ErkJggg=="/></td>
         *        </tr>
         *        <tr>
         *           <td>Processing response</td>
         *           <td>"PROCESSING"</td>
         *           <td>{{#crossLink "SourceStatus/PROCESSING_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADbklEQVQ4T21TbUxTZxR+Lr2FKm2xDYod1ZasgKSzkbSCia1KouimkogLP9YRNkt0kGVKf2hYBAZ+BJVoJEqi/vAjmYSJLiSDfUAQBomDLBCNRWpFxUxp7d0ovY29LZTutFmNyXyTm/ve957nOed53nMYvGfV1v5g37hxfb1czi56PK/H2toazt69+1PP+2KZxGFVVVsux02EZ2c9kcLC8l+i0Wie0ZgHiyUbjY1nxnp6uk6ZTDu4gYHWUa/XG0jg4gSdnfcrCgq0bYKwgImJmajb/SZVJAI2bMjBqlVpePTIjf7++xCJotFgkBtpbq6pCIW4xzFsnODo0RtnGSa1RqfLQnFxPiKRCORyEcLhMPx+AQzDElgMlmXx4oUPNlvZBYejz07Q+ThBSck3O3JzN/0sFotgtW5FTo4UTqcXHk8SJJJkcJwbwSAPnU5PlQzM19eXHRcE/ymChuIEjY3dJ1UqVa0ghFFaakAoFKRMKZQ1CT6fF3q9mmS4MD3NU/TC4pEju6sDAe5SXEJHx/iVrKwPrRw3u4Q0wmxWY2zMB0FgUVdXMcLzkozy8s+1NtvHuHlzGEqlCr29t8dHRn695nQOXmd4PmYM4Hb7oVbLkZQUgsvFYGioj7fbd1Zt315Tkp1tKGtq+gy3bv1BkpZDpcrEvXvDaGr6dC9z+vSNTqNx897k5BRotQoyD1SuQBKeBVtaGs5ZLFbr6tVKzb59Zly9Ooxly9SYm/sH1BfBrq4z1TEPluzZY281Gj+pzMxUkgfr8ODBc6Snq8lED3ngJ1kaPHnyEoODfyEtLY08q/htZsbRTtihuIknTvy4n8q8FAgEsGaNDgpFiACvYDBoqSIWDsdz9PU5ycx1pP+O//LlmoMEuxY3ccuW79jKyvJuvf6DYp9vjo6kWLEilTJz5MUz8Pwb6guW5MXI5HSNvaGWlgM2QeC+TzQSYzZ/fUEmy6o2mT7Crl3bMDU1RW4rIJWm0pWGsbAQpYpcWLpUiocP/8TFi4e+FYS/m4kgmpgFZUaG4SvSbdFqNcuVymyDTKYQFxUVYeVKFc6fb3g5Oto/KZFIJW73pMvvf9VK4PG3rfzOlMloL1+7tvhLjcZ0MD/flE4Lx4590c5xTw/TPyHWSfT4Epi30/gOyX9bcQENVB59pEQi85P0/v3/McC/d5diHn407noAAAAASUVORK5CYII="/></td>
         *        </tr>   
         *        <tr>
         *           <td>Response decoding</td>
         *           <td>"DECODING_RESPONSE"</td>
         *           <td>{{#crossLink "SourceStatus/DECODING_RESPONSE_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADWklEQVQ4T2WTbWyTVRTH//e5z3vf17mx1naZTrYSAWHgjHG+BFBxhAScGoHwsim6hERjIiEhRKiBL76giUZNigEXMZpoRCSAH9xCYAkUNvYCjBYZ26qrbZG2G23XPuvj3YNriNzc8+XmnF/u+f/PIbj30NZPn3vR66lttJdbG3W9OBlPxS/2n+3/6de958/9P53c/bBy34KWsjm2j6Ws7CkgD0o1UJGDVK6C8hTTYv702FDsraPb+ntm60qA5g8XHCTQN+VuZ2GplSCYKSYTGhzVEtQyAYRlpoamUFHnTI91pVf8/M6d3xiAZ99/uG26oAU0MgWrV4agcnBPNGDt3C34LXwEiYZu441nQaJmcKZi6nQgtDi4f/Q6md9uc7irvMPJv5M2e73CEim8+SVo8bWC4zj0jfZiYN7h/wAUbnMN/rwcRV6bPvzl053rSfOeRW3J6K2AXM1DMHF4AI/ilYWvQ+AF5LQcDkR2Q63PlQBey0PI/6MhlhnHma/DD5KV/ke+vXUjsc5cJ6NWbMSrDVshiqIR3wcPImOPMiEJOJ61IFI4PCosNQQjAxHc6E68TJa95zuVy2ebnLIHbzy2Aw6rA7IkQ5RECIIAnueNVqDPXB1XIoMYX9qJWCyOsaFxP1m+x3cqm2UAxYN3l+2DLMsGQJIlSCJzg0FmALquMycILg73IL24D6EoA12N+ckL/oUd6Whqw4wGdcrjaGt6G4qsoKAX8GOwAylLxPi601wBk2KCVDkNd5ULXRdO4tq5SAtp9i9q5TL0QMaeNESsl5rQ/tR2KAqDFAvYG2xHpU+BzeyAVbXDpdYgGZ3AwM1uHPu8r3xmDmh7YM1wOBzy8PcXDBt9/JPY9sxOqKqKS2MDOKMewn3mKjjVSkPMcCiEvzIjHfubjm40BmnzJ8+/VGF3/XAl1guLS4bZYsJcPIF1jVvRM3IWadc15grTQgCuD0agWSdSJzo6a7s+CiVKo7z9m81vSrz8RSGjoXqeB6qFtTClw1TGXGA23k5mEY/HwJcXU6HekeVfbfzlfGmUZxfjtc9Wr3BW2QKYpF7ZIQGTzHumS1HUwNuAqWz+u2B3/67juy78cc8y3bWVZNXOJUvddZXNWrEgEEp0SunNcO/okd8/GCwVzub/C1MgLl8VCbqbAAAAAElFTkSuQmCC"/></td>
         *        </tr>
         *        <tr>
         *           <td>Query completed</td>
         *           <td>"COMPLETED"</td>
         *           <td>{{#crossLink "SourceStatus/COMPLETED_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAB4ElEQVQ4T2NkoBAwkqo/aJucNcPffxt/vPsvvC3+qQhJBoA0MzMzbHx3+7fwj7d/jh+tfx1MtAEwzb++/hd+e/n7ye93OU6cXfSkligDYJrFFEWF7x1/wfBqL+PEF3v/T3v69OlDggbANCupKQlfPnSb4efnP8deHfk3+/KK12uB4fcZxQCY4r9/GfzXeT06CuObatkKHz11kOHTw99X35z61wPV/AtowE+4ATDFaWZNwrNO1b0FGQIKMJDmK6+PMry+8pvh1032vr2t99qAGr+ANINiEGwAsmZYtIIMAWm+9/0iw6Pj78FO35vzMgQo/w6qBmFA6E65NyCbQRJvfj+BJ41v/z4xbDi4lOHvDwaGPzd4Qne139yPbDvcBSADGP8xcGhp6XNzMfHBDTh96wjDixPfnrHzMd3fk/syFKr5M3LiA3tBWlpazaCUvYFb/Y/fz2//mUV0OTj+fP/H8O72T4bvT3/fP1L+0fHHjx8gp4MDDsMAoAA70BB5HuPv+nJaIuY8Vj+KWXmAIfXkL8PvB2ypu+vur0d3OswQ5GjkhbpGUiuWO+2nwIdiIQ22zxsCnqhjczo2A0Bi7EDMBnSNJNDJAn/+/Hn78eNHUKiiOBubF5DFYGyQi1ACDJsiAIU/4VHlwvAFAAAAAElFTkSuQmCC"/></td>
         *        </tr>
         *        <tr>
         *           <td>Error occurred</td>
         *           <td>"ERROR"</td>
         *           <td>{{#crossLink "SourceStatus/ERROR_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADZUlEQVQ4T22TbUhbVxjH/+fm3tyb5N6YBtNkGndJfWmrcVqT6dBWtNUycBXW+qGoUJlQV6lbKaPr29Yvc1/GEPdhFMEKgzIo3Sxbi3RtSt0yRTojWGkjTZWqGSFMrY2makzcY7a+bgcOB855zu95/s8Lw/+tt6u2VCva3ZZgUGVIIGGxzX8fjf0E3+3A6+bslQv6+GEk1OlYXHhPkUTYbDaIjCEaCmFpeQWzkvzbNZ3u2K3RYd+zf88BxWXV7r2T92+aHGrK9uZm2F0uGI1GcByH1dVVRHw+BM6fh+J/8KTPuPnYN+O+ng3IPwDX7sxPg+PDqZXlKaVHj8JisYCLxcDCYfAE4O128LKMlbk5DNXXQ5j5E99mZLt/8XmHk4DG7Nx+1WIqLz93DmazGes3bmC9rw+a+XlwwSAEgsmHD0N34gTW6G6wqgohSbnb9tBfwDa8t4YDgeKPP0J6QQHY4CAyamuh1WoxU1cHSaPBm1euYPnpU8QuXIDx5EmEL17E6Gefozm/tIRV7txzvCw8/XXO2bOQKRqJHje3t0NRFMQXFiDpdFAomUskJ1xUBCs5SBDc43RiyPXOF2xXlrO9xGY6vfXQIUiPH0Pu7sYaaTafOgW9Xg8dAbjlZcw2NYH3+2Hu7YWQl4frOTkIO3f8TIDc9qK01NOO/fshkj5DZyc4kwmLbW2QrdYkhF9ZgXjmDNjEBFIoQpEAnsJCzDsL+1lFWdUn7tDkV7YjR8CiUShdXQg1NiIhSbBSGUWCbawNeWJLC1IuXQJHFblTXY0Rp/tLhpK9rqaHI3/kNdQjkZYG/egoZkmCjjyn9vRAVFVEDh6EngBGrxcySYl4PPB3dOC74oq6ZBlrs3K9qsjKHNRAa0tLkAYGwNEWKIkaeufT0yGUlsLQ0AA+HsdgayuikmGq49GEIwnYWlzhdk/eu/PGW/mw79uHOBnxJIefnobA89Bu2waRmotPJDBG1UrMzqE/y3ngV9/vPz5v5Z3OHR+oUxPdkkGP7JoayJQkDfWAIAjJHaHyBS5fhrgaw9j2/JZrQ96uF63872Rs2fXunsLpB92GUFDVUumMmZkAnYvUjZIoYtFmnxo3W48PjAz88J9hemkqmVpe8372XzOVmxjLZxT2k3U2NpmecXvcc7WX7OIvT/Df1VMsmVTz7HQAAAAASUVORK5CYII="/></td>
         *        </tr>
         *        <tr>
         *           <td>Source unreachable</td>
         *           <td>"TIMEOUT"</td>
         *           <td>{{#crossLink "SourceStatus/TIMEOUT_DATA_URL:property"}}{{/crossLink}} </td>
         *           <td><image style="background:transparent" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC2ElEQVQ4T11TTSisYRR+Xn8zTMICJaWoSZLBKErC2DALZWjEVdYspCykLKynGcrGQrJANIpm5GM1otQ0o+YzfjaIQoZYjMQdjO++59xG133rq9P3vuc853nOcwT+OYqi6D4/Py1fX1/K1dUVzs7OoGkaSkpK+JOxVafT+axWayyRJhKBz+czPTw8qDs7Ozg5OUF6ejpfxeNxpKWlIRqNIj8/H52dncjNza1sbW09pHsusL29bbq8vFRnZ2dRWFiI3t5e1NTUcCwRcX19jWAwiNXVVVBn3d3dMJlMlRaL5VBQ24+Pj79dLhfa29vR39+faBeSCoQQSEpK4m4kCObm5iBzMDY2hqqqKr1YX19v29jYUJ6fnzE1NYWCggJ+TNzpvL29Qa/Xc6Hk5GTc3t5iaGgIOTk5aG5utoqVlRXN4XBgdHQUdrv9G5USqAM61MHr6ytrkZKSgrW1NUxOTnIhMTExofn9fiwuLiI7O5uR6dH/J0GHCt/c3KCnpwc2mw1icHBQOz8/x9bWFicTGj06Pj7G7u4uqqurUVpaiszMTKZAhT4+Plgv+i+Gh4e1QCCA/f19vL+/MzoV2dzcxPLyMurr60H6FBcXo6WlhbkTUFNTE+rq6iCmp6c1t9vNI8rLy2N0+o6OjnB/f4+KigqEQiEYDAaOMzIymEJfXx+6urogPB4PizgyMoKOjg42DiGoqgo5XkjD/JCDutzb24PUjnOERG5bWlpSqHWn04mioiJOIA2enp7Q2NiIl5cXpkXod3d3kLpxLAGt30aamZmhubKRjEYjSNhIJILa2lqkpqZy0dPTUywsLLCRxsfH/xqJLmgPwuGwOj8/j7KyMva72WxmwbKystiB8p51Im0GBgZQXl5e2dDQcPhjmWTLqtfrxcXFBSMSMo0uFovx+Ige6STdysnfy5RQSa6vTopnkY5TaIGIL3GnLZRr/kuO3H1wcGCQixZN5PwBnSVjy2IX6jwAAAAASUVORK5CYII="/></td>
         *        </tr>
         *     </body>
         *  </table>
         *
         *  @return {String} A String with the name of the phase of completion or a data URL of a correspondent predefined icon
         */
        phase : function(dataURL) {

            if (dataURL) {
                switch(status.phase) {
                    case 'INIT':
                        return this.INIT_DATA_URL;
                    case 'DISTRIBUTION':
                        return this.DISTRIBUTION_DATA_URL;
                    case 'VALIDATION':
                        return this.VALIDATION_DATA_URL;
                    case 'DECODING_REQUEST':
                        return this.DECODING_REQUEST_DATA_URL;
                    case 'NETWORK_WAITING':
                        return this.NETWORK_WAITING_DATA_URL;
                    case 'DECODING_RESPONSE':
                        return this.DECODING_RESPONSE_DATA_URL;
                    case 'JOIN':
                        return this.JOIN_DATA_URL;
                    case 'PROCESSING':
                        return this.PROCESSING_DATA_URL;
                    case 'COMPLETED':
                        return this.COMPLETED_DATA_URL;
                    case 'ERROR':
                        return this.ERROR_DATA_URL;
                    // case 'CANCELED':
                    // return status.canceled;
                    case 'TIMEOUT':
                        return this.TIMEOUT_DATA_URL;
                }
            } else {
                return status.phase;
            }
        },

        /**
         * Icon data URL of the "INIT" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property INIT_DATA_URL
         * @type String
         */
        INIT_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADKUlEQVQ4T21TW0gUURj+zszszM7OurvOupar5m4rrZZam21WFoViFBQ9BN2oMJIosBChQopKeunNhx6KiIQigi4PvkhFdyGDoqCLBLJpdHHV3JtuOe7OTGemFKEGPv5z+b/vnPPP9xP856s4UB0o9gXqJpi0j2MJES1ifCQSffrqfM9Lmq7PppDZk+rDdYFAme8sxzM7oWjgRQFE16FaNaiiDk7lXvz8nG69dfRq7zRvRqDh9NZl3kLPAyYNp9sjwyZLIBzd5mgqCyhZBUPDUVhcApQfmUPX9l64aIiYAsGm5f4loco3OWnBWRAoAHgClagYy8SRVJNIqCnEtDh+YRKZEQU1vjBG+5Ibu9tu3DUF1p7ZdLWAce8pDQbMEyem0ljpDWNZUQgsy+JjvB+XI9fQm6IlyABSworykoVfunZc95M5e4L+ed7CT7XF1bDl2RFNRNFYtRP5cj4YhoHFYjFhjFt7T+DZEH2+oiOf8YD9zm4jlUdW7RM14cr6mnWIxAYRkiuwoiQMQgisVitEUTSjgeHJUWzu2gE9q4NVGMgWz0VS09bQLmr8qQpfEB+H+7ElsAF5nGwSjZMlSYLD4TCjMd/bfRCR+AD0KcBpz+0h4eP17XaIp1xSDlKZcazODcPN5cJms8Fut5tEA4Ygz/NofnQMA8nPQBZUWO4hVS1rWtiE2iHn50KnBfSzRShzlM4QDaFpcjI7juanxwCNVn5Sx1zZe4nM2bVgvtvmisicA0Ri8G30G5oqdkO0/Hm/AUEQzHjhbSdej70FVJ2uichj8prM31jZsvqJ8im1Vi52o38kAqeYgw3z67HIEzSvHcsmcP/rE7yL9ZlG1tIq5gaLku9PPveYAt79i0OyVXqsDKacMS5JvUCoCw0H0m3qQmLEv57VMxpcXjf4jLC9r+PFzRkrLzq8olFX9M7RgSGoFg1EMMhUiKFCBgzbKoDL74FV59vedfSem7HydGOUHqhe6nI4O7kJVE39VPArOwmd0WCl9ZBcdmRy8GUinm7tO//89j/NNLsrQ0fW1JeXlDW4nXKtwPFkbDz24fuP6MN77Xfu0Dx1du5vpjUHBLzx3/8AAAAASUVORK5CYII=",
        /**
         * Icon data URL of the "DISTRIBUTION" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property DISTRIBUTION_DATA_URL
         * @type String
         */
        DISTRIBUTION_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAOCAYAAAAmL5yKAAACPElEQVQoU2NkwA44GTyLznKr+mh+vb3lOsP2PmOgsu/YlDJi1a9u0cHr2VX+9+8vBmZmNobP28s6GW6eqCDeAIeU83yGCQZ//vxgYGHhYPh0fsEFhgNzDIk3QMWwkd9vSt2fPz+BBrAzfNyU08Rw53w98QaAVGrZ9LLblRf9PNTZx3DtSDGOsGJADgNOBnZ2GaDC/ww/mX4w8HBaMPq3rv6/sTqU4cv3Ewzs/ziAcowMP38+QQ5QmAFiDEFNB1kEVDT+/fn1H2wbEzMDO5cI489vb/4z/PsLEWJhY/zz4c4NhnV19kDuK5AYxAA2Hi2mqGln/63O9QGL/f37m4GbT4/Ju3nSv621eQxfP10CRgcryHVMoZO3/FuWZczw68s1mAGiDFp2dex2ZZk/t1eWMTy83Ac2VMO2m92hrOTnga4ehhuHS8Fi8rpF7J7tXT8PdU1nuHaoCSjympHBo/gOj7qv8v///8Bx/mllQgoDG6cIf9CMDngsrMuoYPj1/Q1f+II5oLTByMjE8OXm5rsMO3pVGLnz9v0HaQYBcJyfmLCXgZmDn880wwSeDk7POMPw98dHPosCZ5AY2OlAQ75OcmJkZPCrvcUpa6PKCAw0JiYWhi9rMgsZOLn5eX36GuApcUtRA8P3rx95Qqb3//v3h+E/MFC/Pz5ym2FTsxojA4+AA4NT7ixuBUfVr1dX3WTYO8MEaMEvYF64gJQXDEBBzeCccYZbO0z964P9txn2TU5j+PLhAPa8AHYkcQAAFZXhD6YckjcAAAAASUVORK5CYII=",
        /**
         * Icon data URL of the "VALIDATION" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property VALIDATION_DATA_URL
         * @type String
         */
        VALIDATION_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC7ElEQVQ4T3WTW0iTYRjHn+/TzePmZO7g3GDTqbPZSIiwwFJsmXgjhV1kB2WywC7ydBGTaCYkCFmYSexGM2jEiLTw0FLTmlB5SNfKabVWqJjbch72be5z3/oUFU194b15eP6/9/nz/F8E9j8ihUIRK5PJWDqd7qfBYDCRra7/25G99BUVFZVKpbJSJBIRXq/X7na7AzUaTZ9KpSoh++3bNbsAWq02Jzs7R+f3Ew4URbCgoCDwA1BmZ/9w6+ruaO/X1xeTgJVNyC6AxWLpCg+nSX0+AgsOpgLuI5huzB2J4zhqNI5h6pu3Mo3Gkff7Avr6+mcEAsEShUIJRQMCWCgaEEQQPsDxVXC5lqG69t7nJFHScbW61LkG2TWBXv+6jcvlpgVSqJF0Og1QBAUvjoNnxQO/rFYwmi3g8nhGgUJkqEtLnTsA11U3CpaXFu9eyD/PoNHpsOYfQRDAMAzIiaCzSw9H09LBPDEBjY2NbR/f9eRuAcqrbjd75x2Xi4uvQsfLVhCL4+CkXL5udcHphNa2l5AolUFyshQmJyahqqYGTsizyzYBCJvNbr5YeOUSmxECkgNSWFhcBBaLDaQVWCX8wOXx1yeiUqkwNDgI7R2dlt7erjPbLfAThOyhaGYEJ16aAlMOD6yQvosURSDPOg0+ggC73QFfx8fhQUN9y9sevYocbmYLIOaFprCYUSMxvGg4nJkHNvtfmJ6eAgGLgLyMYHj8JgRGTd9s9jmbatw4/IQUY1tbEDKAERYR8UmSKBF+GB57deqsIosXzQIpxwpCLgqmuXgYMzu7mx5pyl3z819IoW9HDiQxYU0JEmmBY/pHy4DZUcHhcBqulZWcOygi4PscAzP0G6qf6bQPSdH67ndEOYEXmhvDFzynUf2WF4bJtDVf5GUfST02kF+odNfWqJXTVuswWcP3+jfIIXHUfFyskDphHs81/XZ1k01k9NcDxt/IvG2jtpcekNRkYTqTivHbR+ae7vfKnsqN4j/7KSLQfZStLgAAAABJRU5ErkJggg==",
        /**
         * Icon data URL of the "DECODING_REQUEST" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property DECODING_REQUEST_DATA_URL
         * @type String
         */
        DECODING_REQUEST_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADU0lEQVQ4T22TWUxTaRTH/1/pxZaW1lhaKAWxVqTCWJUiIHGZ+OCSkWjc4hKNOiSExAeXmJi4jGZi4gtGo4k+gBHimBlEfZghmnGJ0QdFlGiMKIyOUlahLN3uvvj1jhIS5yYnN/fec373/P/nOwT/cwWXH/JNzw+slSXVoRGFMEaDNNj3T8uTv0610XRtcgmZ/FCx+qivqHjpr6mmtK2aEkEKEaEqCaiwQuDHkvdQZPRz1a26vXe/1U0ANlQ3luT55t3T1LDdleGC2TIVmiJCYkdAGBM4NoreUAeMTBp4Tqq5XLv7UhKiA4I/HswoX7H9vYWJ2nO8cwFVgiSK8Nr6URbMQ921p3jWBbDxGPh4P4r8LgwMRze3XNl3XQds2veoMdtt3pGfXwBVlTEUZvF5OI4ti1kEfsgFy7I4feEB2t7KUGQWROnDLJ+vp+lStZd4/DsdZatqwiWBDKTbsxDqZxGJ0ojEkBh6i7NHS8EwBnAch9qLD/G8U4AisbBaxqlEy8+kYl3thuyZZc3Ll5VgoG8ABnEQiQSH8EgcCvXAbYth+8YkhFAIj/P1j9DexVOJ3bBZ3VdJZc2dk85M/3G/PwuFtnYEF3ihaRoIIVSOCkmSIAgiDR48L9DgcaL2PgZHOJhMpsfkp+o7J7Ny5hx3OF2o8LRhbmEOLf7P3yRAlmUdIorJYgFjo1GcqXtKATymmFIfk2Ubz25y5JY3efICUOL/IjHWQ42i86dmaqoIp3kcO7eUUoCoR/3vL/DqvUjldMFhn3GNzF64f2ZB+a4PTpcFTOo0dHfHKCChu53Cv8PhPbkwmY20CxmX/3iJ1x+hmyiJIUy1ZVXpza6uuv9byhRmW6anCH291LyvgDWBDhTPz9OlNDR34E0oVQez0U46sczIUO+gVwcku/CX726HxtoF1Q31K2Ce8wUqV85G481OdPSm0/csuFgPnYgGo8FY9fzvX+onjvKSdWdKbZ6ld0eHO23mdD9dGQ0SNwp+vBtGawYUMQ4+8YE6n0a/kYZnLYd2TRzlb4sRrLxQ7M4uOGdkhMWKIoMTwrR9ESmKQv8KSHxaT3Ss/0Dr7WPN3y3T5K1ctOZIaWFg4fpMh32+gcAaiYy0fvoUav2z4cANmqdMzv0C2tGZc8nM+tEAAAAASUVORK5CYII=",        
        /**
         * Icon data URL of the "NETWORK_WAITING" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property NETWORK_WAITING_DATA_URL
         * @type String
         */
        NETWORK_WAITING_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAACMUlEQVQ4T62TS2gTURSG/5kkk2RaCUkfxle1rRpcxNrUVUHBRYoUuqqbikXBRQhJC4IogkIFxY0ItonRUsSFKEgw1AdCKyJIfay0Dx/VvKTJpBgnsTFpMpnMxLFilEljNt7Vvedyv/vzcQ4B2bLb7QMkSfbI6/KzKIr3PR7PCCG/cDgcEy6Xy1oN4HQ6J91ud1dFgNE6BEKhKuMoaQMW7tpRFbC++wKUdGMZgK5V48ONQ/8RsIo0i8lkqnvyJgwoqFIChtMhlmlAWYJq0pjkMr7lebz7/AmpbA5+hsRBSz2ujgz/kvgbIJdGSHr3d+3AmaNWEEIei3E/WJ5GRNiM2dfvwUyMBn23rreWAHJpbWY9Bg4U8YWdAa22gKIaEY5HEMl2IMlp4H/2Ev6H5/srAo70taNzewDTwTvQUM1AQYf5UAzh7GFo6Rok56X91EVvRcDxwR7oVD7Mvh1HQVBAS2gR5feAFbshxFkkAimwfl+sBGjqHcMaPV2yvq9zNyBeQTTyCDRlQOE7BcF4GlBvQ2KGgZgnpFrwD0DeMcMPQnj8MYRM9CSUMKBGdwoc34J0QpAScWjbWYuvwRde4u8+MJvNepvNJn0NvJoO4MQ1AkVyCXzOC7WqHdk0CZFYwKbmjWjasgupOV//yiys1gtFqX773hwujWpQFBoA/VOQCgdyywzqDYPYatx7duxc79AK4F8jvMTXrQtGOwoZjlpbUB9DejExtcHQN/588vLNn29/AOPlFct62uwRAAAAAElFTkSuQmCC",
        /**
         * Icon data URL of the "JOIN" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property JOIN_DATA_URL
         * @type String
         */
        JOIN_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAOCAYAAAAmL5yKAAAB/0lEQVQoU6WTS2jUUBSGv3QycdpJE4Xqyo2PqvVFtV2ICxVFtOiiCONrUQQf4EawvgpCrV3VVwU3gg+QLlQcEBdKFVHQhbhotfiqtlU3rqQLEzPtNJNpPMk041AqFEzu5ZI/53z33D8nCv95KWG+zobKbVxTF1LtvuFLppt6UV29kb74MmpyH+l3HlArmpZsoEdbzWJviMHfjzgUAip3MaDOo5oyUGT+6uJoWQLT2E2b74mmgn2XtvEs1swmLvvjkiTT+85gCJh1Gl/u8FLiMPKcZ5Jklq+j3s8VtNGX9AjMqtjIpkArBIdDTtDIkLacBb5Agt2smxxQNKqMJjoigN1Fi+8ybO7nRliVZLof+FrwAGbHV9Ca3MJh5z4nvW90BqJALyS3cjzzmIsSfCLQ1Pk06zs4n3nC1dx72iOARLPUOEivfYvtYXF5ciRZaaa4YqU5QoZ3xJDD4Bv7eGhfpw6XT38BMEffw4tYFUuk7IIjMRk6St6R53zRIyU/zGfnDutF+VkKCCLKmcHcYBfGyGKyxthJ2r5HCovX8i4RVjfGD1lHJ3yccGGKRXrgkt5As9NNp/TCsakiJ1dQjFEXcdbYS2vxK9ym3RvgzGTIPwEVm3mbWEttBMi+om/kKaumDdBq6NBTnIo60Ulzzu2nZdqAwFBpsN6Sf6EuMq4U8gdpTqhx8Av8wQAAAABJRU5ErkJggg==",        
        /**
         * Icon data URL of the "PROCESSING" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property PROCESSING_DATA_URL
         * @type String
         */
        PROCESSING_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADbklEQVQ4T21TbUxTZxR+Lr2FKm2xDYod1ZasgKSzkbSCia1KouimkogLP9YRNkt0kGVKf2hYBAZ+BJVoJEqi/vAjmYSJLiSDfUAQBomDLBCNRWpFxUxp7d0ovY29LZTutFmNyXyTm/ve957nOed53nMYvGfV1v5g37hxfb1czi56PK/H2toazt69+1PP+2KZxGFVVVsux02EZ2c9kcLC8l+i0Wie0ZgHiyUbjY1nxnp6uk6ZTDu4gYHWUa/XG0jg4gSdnfcrCgq0bYKwgImJmajb/SZVJAI2bMjBqlVpePTIjf7++xCJotFgkBtpbq6pCIW4xzFsnODo0RtnGSa1RqfLQnFxPiKRCORyEcLhMPx+AQzDElgMlmXx4oUPNlvZBYejz07Q+ThBSck3O3JzN/0sFotgtW5FTo4UTqcXHk8SJJJkcJwbwSAPnU5PlQzM19eXHRcE/ymChuIEjY3dJ1UqVa0ghFFaakAoFKRMKZQ1CT6fF3q9mmS4MD3NU/TC4pEju6sDAe5SXEJHx/iVrKwPrRw3u4Q0wmxWY2zMB0FgUVdXMcLzkozy8s+1NtvHuHlzGEqlCr29t8dHRn695nQOXmd4PmYM4Hb7oVbLkZQUgsvFYGioj7fbd1Zt315Tkp1tKGtq+gy3bv1BkpZDpcrEvXvDaGr6dC9z+vSNTqNx897k5BRotQoyD1SuQBKeBVtaGs5ZLFbr6tVKzb59Zly9Ooxly9SYm/sH1BfBrq4z1TEPluzZY281Gj+pzMxUkgfr8ODBc6Snq8lED3ngJ1kaPHnyEoODfyEtLY08q/htZsbRTtihuIknTvy4n8q8FAgEsGaNDgpFiACvYDBoqSIWDsdz9PU5ycx1pP+O//LlmoMEuxY3ccuW79jKyvJuvf6DYp9vjo6kWLEilTJz5MUz8Pwb6guW5MXI5HSNvaGWlgM2QeC+TzQSYzZ/fUEmy6o2mT7Crl3bMDU1RW4rIJWm0pWGsbAQpYpcWLpUiocP/8TFi4e+FYS/m4kgmpgFZUaG4SvSbdFqNcuVymyDTKYQFxUVYeVKFc6fb3g5Oto/KZFIJW73pMvvf9VK4PG3rfzOlMloL1+7tvhLjcZ0MD/flE4Lx4590c5xTw/TPyHWSfT4Epi30/gOyX9bcQENVB59pEQi85P0/v3/McC/d5diHn407noAAAAASUVORK5CYII=",
        /**
         * Icon data URL of the "DECODING_RESPONSE" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property DECODING_RESPONSE_DATA_URL
         * @type String
         */
        DECODING_RESPONSE_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADWklEQVQ4T2WTbWyTVRTH//e5z3vf17mx1naZTrYSAWHgjHG+BFBxhAScGoHwsim6hERjIiEhRKiBL76giUZNigEXMZpoRCSAH9xCYAkUNvYCjBYZ26qrbZG2G23XPuvj3YNriNzc8+XmnF/u+f/PIbj30NZPn3vR66lttJdbG3W9OBlPxS/2n+3/6de958/9P53c/bBy34KWsjm2j6Ws7CkgD0o1UJGDVK6C8hTTYv702FDsraPb+ntm60qA5g8XHCTQN+VuZ2GplSCYKSYTGhzVEtQyAYRlpoamUFHnTI91pVf8/M6d3xiAZ99/uG26oAU0MgWrV4agcnBPNGDt3C34LXwEiYZu441nQaJmcKZi6nQgtDi4f/Q6md9uc7irvMPJv5M2e73CEim8+SVo8bWC4zj0jfZiYN7h/wAUbnMN/rwcRV6bPvzl053rSfOeRW3J6K2AXM1DMHF4AI/ilYWvQ+AF5LQcDkR2Q63PlQBey0PI/6MhlhnHma/DD5KV/ke+vXUjsc5cJ6NWbMSrDVshiqIR3wcPImOPMiEJOJ61IFI4PCosNQQjAxHc6E68TJa95zuVy2ebnLIHbzy2Aw6rA7IkQ5RECIIAnueNVqDPXB1XIoMYX9qJWCyOsaFxP1m+x3cqm2UAxYN3l+2DLMsGQJIlSCJzg0FmALquMycILg73IL24D6EoA12N+ckL/oUd6Whqw4wGdcrjaGt6G4qsoKAX8GOwAylLxPi601wBk2KCVDkNd5ULXRdO4tq5SAtp9i9q5TL0QMaeNESsl5rQ/tR2KAqDFAvYG2xHpU+BzeyAVbXDpdYgGZ3AwM1uHPu8r3xmDmh7YM1wOBzy8PcXDBt9/JPY9sxOqKqKS2MDOKMewn3mKjjVSkPMcCiEvzIjHfubjm40BmnzJ8+/VGF3/XAl1guLS4bZYsJcPIF1jVvRM3IWadc15grTQgCuD0agWSdSJzo6a7s+CiVKo7z9m81vSrz8RSGjoXqeB6qFtTClw1TGXGA23k5mEY/HwJcXU6HekeVfbfzlfGmUZxfjtc9Wr3BW2QKYpF7ZIQGTzHumS1HUwNuAqWz+u2B3/67juy78cc8y3bWVZNXOJUvddZXNWrEgEEp0SunNcO/okd8/GCwVzub/C1MgLl8VCbqbAAAAAElFTkSuQmCC",
        /**
         * Icon data URL of the "COMPLETED" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property COMPLETED_DATA_URL
         * @type String
         */
        COMPLETED_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAB4ElEQVQ4T2NkoBAwkqo/aJucNcPffxt/vPsvvC3+qQhJBoA0MzMzbHx3+7fwj7d/jh+tfx1MtAEwzb++/hd+e/n7ye93OU6cXfSkligDYJrFFEWF7x1/wfBqL+PEF3v/T3v69OlDggbANCupKQlfPnSb4efnP8deHfk3+/KK12uB4fcZxQCY4r9/GfzXeT06CuObatkKHz11kOHTw99X35z61wPV/AtowE+4ATDFaWZNwrNO1b0FGQIKMJDmK6+PMry+8pvh1032vr2t99qAGr+ANINiEGwAsmZYtIIMAWm+9/0iw6Pj78FO35vzMgQo/w6qBmFA6E65NyCbQRJvfj+BJ41v/z4xbDi4lOHvDwaGPzd4Qne139yPbDvcBSADGP8xcGhp6XNzMfHBDTh96wjDixPfnrHzMd3fk/syFKr5M3LiA3tBWlpazaCUvYFb/Y/fz2//mUV0OTj+fP/H8O72T4bvT3/fP1L+0fHHjx8gp4MDDsMAoAA70BB5HuPv+nJaIuY8Vj+KWXmAIfXkL8PvB2ypu+vur0d3OswQ5GjkhbpGUiuWO+2nwIdiIQ22zxsCnqhjczo2A0Bi7EDMBnSNJNDJAn/+/Hn78eNHUKiiOBubF5DFYGyQi1ACDJsiAIU/4VHlwvAFAAAAAElFTkSuQmCC",
        /**
         * Icon data URL of the "ERROR" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property ERROR_DATA_URL
         * @type String
         */
        ERROR_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADZUlEQVQ4T22TbUhbVxjH/+fm3tyb5N6YBtNkGndJfWmrcVqT6dBWtNUycBXW+qGoUJlQV6lbKaPr29Yvc1/GEPdhFMEKgzIo3Sxbi3RtSt0yRTojWGkjTZWqGSFMrY2makzcY7a+bgcOB855zu95/s8Lw/+tt6u2VCva3ZZgUGVIIGGxzX8fjf0E3+3A6+bslQv6+GEk1OlYXHhPkUTYbDaIjCEaCmFpeQWzkvzbNZ3u2K3RYd+zf88BxWXV7r2T92+aHGrK9uZm2F0uGI1GcByH1dVVRHw+BM6fh+J/8KTPuPnYN+O+ng3IPwDX7sxPg+PDqZXlKaVHj8JisYCLxcDCYfAE4O128LKMlbk5DNXXQ5j5E99mZLt/8XmHk4DG7Nx+1WIqLz93DmazGes3bmC9rw+a+XlwwSAEgsmHD0N34gTW6G6wqgohSbnb9tBfwDa8t4YDgeKPP0J6QQHY4CAyamuh1WoxU1cHSaPBm1euYPnpU8QuXIDx5EmEL17E6Gefozm/tIRV7txzvCw8/XXO2bOQKRqJHje3t0NRFMQXFiDpdFAomUskJ1xUBCs5SBDc43RiyPXOF2xXlrO9xGY6vfXQIUiPH0Pu7sYaaTafOgW9Xg8dAbjlZcw2NYH3+2Hu7YWQl4frOTkIO3f8TIDc9qK01NOO/fshkj5DZyc4kwmLbW2QrdYkhF9ZgXjmDNjEBFIoQpEAnsJCzDsL+1lFWdUn7tDkV7YjR8CiUShdXQg1NiIhSbBSGUWCbawNeWJLC1IuXQJHFblTXY0Rp/tLhpK9rqaHI3/kNdQjkZYG/egoZkmCjjyn9vRAVFVEDh6EngBGrxcySYl4PPB3dOC74oq6ZBlrs3K9qsjKHNRAa0tLkAYGwNEWKIkaeufT0yGUlsLQ0AA+HsdgayuikmGq49GEIwnYWlzhdk/eu/PGW/mw79uHOBnxJIefnobA89Bu2waRmotPJDBG1UrMzqE/y3ngV9/vPz5v5Z3OHR+oUxPdkkGP7JoayJQkDfWAIAjJHaHyBS5fhrgaw9j2/JZrQ96uF63872Rs2fXunsLpB92GUFDVUumMmZkAnYvUjZIoYtFmnxo3W48PjAz88J9hemkqmVpe8372XzOVmxjLZxT2k3U2NpmecXvcc7WX7OIvT/Df1VMsmVTz7HQAAAAASUVORK5CYII=",

        // functionality not yet developed
        // canceled : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAsAAAALCAYAAACprHcmAAAAsklEQVQYV2O8MWPG/w/71zMQAgKOgQyMJ/zt/xskZhJSy3Bh/nSo4shMhg/Ll+PUIBAZyXBhOUixvfl/gchEBoH123Eq/hDoCTRsPpLi5bjd/SEyEKJ4h7n5fwsHB4YfOw7gNJnDw4HhxokDUMUGFgQVn7hwAugMdfP/GgFEmLwBaPIBfvn/Ep2VDALzcYfGh8RIhhfl7RDFFjM7CYbzifRyBsYL8fn/P2zYQFCxQEAAAwD06U815pS2YgAAAABJRU5ErkJggg==",
        /**
         * Icon data URL of the "TIMEOUT" {{#crossLink "SourceStatus/phase:method"}}phase{{/crossLink}}
         *
         * @property TIMEOUT_DATA_URL
         * @type String
         */
        TIMEOUT_DATA_URL : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC2ElEQVQ4T11TTSisYRR+Xn8zTMICJaWoSZLBKErC2DALZWjEVdYspCykLKynGcrGQrJANIpm5GM1otQ0o+YzfjaIQoZYjMQdjO++59xG133rq9P3vuc853nOcwT+OYqi6D4/Py1fX1/K1dUVzs7OoGkaSkpK+JOxVafT+axWayyRJhKBz+czPTw8qDs7Ozg5OUF6ejpfxeNxpKWlIRqNIj8/H52dncjNza1sbW09pHsusL29bbq8vFRnZ2dRWFiI3t5e1NTUcCwRcX19jWAwiNXVVVBn3d3dMJlMlRaL5VBQ24+Pj79dLhfa29vR39+faBeSCoQQSEpK4m4kCObm5iBzMDY2hqqqKr1YX19v29jYUJ6fnzE1NYWCggJ+TNzpvL29Qa/Xc6Hk5GTc3t5iaGgIOTk5aG5utoqVlRXN4XBgdHQUdrv9G5USqAM61MHr6ytrkZKSgrW1NUxOTnIhMTExofn9fiwuLiI7O5uR6dH/J0GHCt/c3KCnpwc2mw1icHBQOz8/x9bWFicTGj06Pj7G7u4uqqurUVpaiszMTKZAhT4+Plgv+i+Gh4e1QCCA/f19vL+/MzoV2dzcxPLyMurr60H6FBcXo6WlhbkTUFNTE+rq6iCmp6c1t9vNI8rLy2N0+o6OjnB/f4+KigqEQiEYDAaOMzIymEJfXx+6urogPB4PizgyMoKOjg42DiGoqgo5XkjD/JCDutzb24PUjnOERG5bWlpSqHWn04mioiJOIA2enp7Q2NiIl5cXpkXod3d3kLpxLAGt30aamZmhubKRjEYjSNhIJILa2lqkpqZy0dPTUywsLLCRxsfH/xqJLmgPwuGwOj8/j7KyMva72WxmwbKystiB8p51Im0GBgZQXl5e2dDQcPhjmWTLqtfrxcXFBSMSMo0uFovx+Ige6STdysnfy5RQSa6vTopnkY5TaIGIL3GnLZRr/kuO3H1wcGCQixZN5PwBnSVjy2IX6jwAAAAASUVORK5CYII="
    };

};

