var router = new VueRouter({

    mode: 'history',

    routes: []

});

Vue.use(Toasted)

var closedStatus = "CLOSED";

var app = new Vue({
    router,
    el: '#app',
    data: {
        items: [],
        emailToSubmit: "",
        confirmInProgress: false
    },
    methods:{
            loadItems: function() {
                axios.get('/wishItems')
                .then(response => {
                    this.items = response.data
                    debugger;
                    if (this.$route.query.reservationSuccess === "true") {
                        this.$toasted.show("✅Речі були зарезервовані за вами", {
                    	     theme: "toasted-primary",
                    	    position: "top-center",
                    	    duration : 9000
                        });
                    }
                })
            },
            allItemsReserved: function() {
                var hasNotReserved = false;
                for(var i=0 ; i < this.items.length; i++) {
                    if(this.items[i].wishItemStatus !== closedStatus) {
                        hasNotReserved = true;
                        break;
                    }
                }
                return !hasNotReserved;
            },
            anyItemSelected: function() {
                var selected = false;
                for(var i=0 ; i < this.items.length; i++) {
                    if(this.items[i].isActive) {
                        selected = true;
                        break;
                    }
                }
                return selected;
            },
            submitItems: function() {
                var params = new URLSearchParams();

                var submittedItemIds = [];
                for(var i=0 ; i < this.items.length; i++) {
                    var item = this.items[i];
                    if(item.isActive){
                        params.append("itemsId", this.items[i].id);
                    }

                }
                this.confirmInProgress = true;
                params.append("email", this.emailToSubmit);
                axios.post('/wishItems/assignItem', null, {
                    params: params
                }).then(response => {
                    this.items = response.data;
                    this.confirmInProgress = false;
                });

            },
            isReserved: function(status) {
                if("CONFIRMATION_IN_PROGRESS" === status.wishItemStatus) {
                    return true;
                }
            },
            isClosed: function(status) {
                if( closedStatus === status.wishItemStatus) {
                    return true;
                }
            }
        },
        created: function(){
            this.loadItems()
        }
});